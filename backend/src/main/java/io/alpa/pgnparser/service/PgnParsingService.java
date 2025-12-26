package io.alpa.pgnparser.service;

import io.alpa.pgnparser.enums.Color;
import io.alpa.pgnparser.enums.Nag;
import io.alpa.pgnparser.enums.Result;
import io.alpa.pgnparser.enums.TokenType;
import io.alpa.pgnparser.model.*;
import io.alpa.pgnparser.util.Tokenizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PgnParsingService {

  private final Tokenizer tokenizer;
  private static final String DEFAULT_FEN =
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

  public PgnParsingService() {
    this.tokenizer = new Tokenizer();
  }

  /**
   * Parses a PGN string and returns a ChessGame object representing the game.
   *
   * @param pgn the PGN string to parse
   * @return the parsed ChessGame
   */
  public ChessGame pgnToChessGame(String pgn) {
    List<Token> tokens = tokenizer.tokenizePgn(pgn);
    ChessGame game = new ChessGame();

    LinkedHashMap<String, String> tags =
        tokens.stream()
            .filter(t -> t.getType() == TokenType.TAG)
            .collect(
                Collectors.toMap(
                    t -> t.getValue().substring(1, t.getValue().indexOf(' ')),
                    t ->
                        t.getValue()
                            .substring(t.getValue().indexOf(' ') + 2, t.getValue().length() - 1)
                            .replaceAll("(^\")|(\"$)", ""),
                    (a, b) -> b,
                    LinkedHashMap::new));

    tokens.stream()
        .filter(t -> t.getType() == TokenType.RESULT)
        .findFirst()
        .ifPresent(t -> game.setResult(Result.fromCode(t.getValue())));

    game.setTags(tags);

    RootNode rootNode = new RootNode();
    int tagCount = tags.size();
    int tokenCount = tokens.size();

    if (tags.containsKey("FEN")) {
      String fen = tags.get("FEN");
      rootNode.setStartingFen(fen);
      String[] fenElements = fen.split(" ");

      if (fenElements.length > 1) {
        switch (fenElements[1]) {
          case "w" -> rootNode.setSideToMove(Color.WHITE);
          case "b" -> rootNode.setSideToMove(Color.BLACK);
          default ->
              throw new IllegalArgumentException("Invalid FEN side-to-move: " + fenElements[1]);
        }
      }
    } else {
      rootNode.setStartingFen(DEFAULT_FEN);
      rootNode.setSideToMove(Color.WHITE);
    }

    if (tokenCount > tagCount) {
      List<Nag> nags = new ArrayList<>();
      Color currentColor = rootNode.getSideToMove();
      int startCount;
      if (tokens.get(tagCount).getType() == TokenType.COMMENT) {
        rootNode.setComment(tokens.get(tagCount).getValue());
        startCount = tagCount + 1;
      } else {
        startCount = tagCount;
      }
      iterateTokens(startCount, tokenCount, tokens, nags, rootNode, currentColor);
    }

    game.setRootNode(rootNode);

    return game;
  }

  /**
   * Iterates over the tokens and builds the move tree for the ChessGame.
   *
   * @param startIndex the index to start from
   * @param tokenCount the total number of tokens
   * @param tokens the list of tokens
   * @param nags the list of NAGs (Numeric Annotation Glyphs)
   * @param rootNode the root node of the move tree
   * @param currentColor the color to move
   */
  private void iterateTokens(
      int startIndex,
      int tokenCount,
      List<Token> tokens,
      List<Nag> nags,
      RootNode rootNode,
      Color currentColor) {
    MovePointer pointer = new MovePointer();
    int i = startIndex;

    while (i < tokenCount) {
      Token token = tokens.get(i);
      TokenType tokenType = token.getType();
      String tokenValue = token.getValue();

      switch (tokenType) {
        case MOVE_NUMBER -> {
          if (pointer.current != null) {
            nags = flushNodes(nags, pointer, rootNode);
          }

          pointer.current = new MoveNode();
          pointer.current.setMoveNumber(
              Integer.parseInt(tokenValue.substring(0, tokenValue.indexOf('.'))));
          currentColor = tokenValue.contains("...") ? Color.BLACK : Color.WHITE;

          i++;
        }
        case MOVE -> {
          if (tokens.get(i - 1).getType() != TokenType.MOVE_NUMBER) {
            currentColor = Color.BLACK;
            nags = flushNodes(nags, pointer, rootNode);
            pointer.current = new MoveNode();
          }

          pointer.current.setSan(tokenValue);

          if (currentColor == Color.BLACK && pointer.current.getMoveNumber() == 0) {
            pointer.current.setMoveNumber(pointer.previous.getMoveNumber());
          }

          i++;
        }
        case NAG -> {
          if (tokenValue.startsWith("$")) {
            nags.add(Nag.fromCode(Integer.parseInt(tokenValue.substring(1))));
          } else {
            nags.add(Nag.fromSymbol(tokenValue));
          }
          i++;
        }
        case COMMENT -> {
          pointer.current.setComment(tokenValue);
          i++;
        }
        case VARIATION_START -> i = handleVariations(pointer.current, tokens, currentColor, i + 1);
        default -> i++;
      }
    }

    flushNodes(nags, pointer, rootNode);
  }

  /**
   * Handles parsing of PGN variations (enclosed in parentheses) and attaches them to the move tree.
   *
   * @param currentMove the move node to attach variations to
   * @param tokens the list of tokens
   * @param currentColor the color to move
   * @param startIndex the index to start parsing variations
   * @return the index after the end of the variation
   */
  public int handleVariations(
      MoveNode currentMove, List<Token> tokens, Color currentColor, int startIndex) {
    MovePointer pointer = new MovePointer();
    MoveNode variationHead = null;
    List<Nag> nags = new ArrayList<>();
    int i = startIndex;

    while (i < tokens.size()) {
      Token token = tokens.get(i);
      String tokenValue = token.getValue();
      TokenType tokenType = token.getType();

      switch (tokenType) {
        case MOVE_NUMBER -> {
          if (pointer.current != null) {
            nags = flushNodes(nags, pointer, null);
          }

          pointer.current = new MoveNode();
          pointer.current.setMoveNumber(
              Integer.parseInt(tokenValue.substring(0, tokenValue.indexOf('.'))));
          currentColor = tokenValue.contains("...") ? Color.BLACK : Color.WHITE;

          if (variationHead == null) {
            variationHead = pointer.current;
          }

          i++;
        }
        case MOVE -> {
          if (tokens.get(i - 1).getType() != TokenType.MOVE_NUMBER) {
            currentColor = Color.BLACK;
            nags = flushNodes(nags, pointer, null);
            pointer.current = new MoveNode();
          }

          pointer.current.setSan(tokenValue);

          if (currentColor == Color.BLACK && pointer.current.getMoveNumber() == 0) {
            pointer.current.setMoveNumber(pointer.previous.getMoveNumber());
          }

          i++;
        }
        case NAG -> {
          if (tokenValue.startsWith("$")) {
            nags.add(Nag.fromCode(Integer.parseInt(tokenValue.substring(1))));
          } else {
            nags.add(Nag.fromSymbol(tokenValue));
          }
          i++;
        }
        case COMMENT -> {
          pointer.current.setComment(tokenValue);
          i++;
        }
        case VARIATION_START -> {
          MoveNode variationOrigin = pointer.current;
          i = handleVariations(variationOrigin, tokens, currentColor, i + 1);
        }
        case VARIATION_END -> {
          pointer.current.setNags(nags);
          flushNodes(nags, pointer, null);
          if (currentMove.getVariationNodes() == null) {
            currentMove.setVariationNodes(new ArrayList<>());
          }
          currentMove.getVariationNodes().add(variationHead);
          return i + 1;
        }
        default -> i++;
      }
    }

    return i;
  }

  /**
   * Flushes the current move node and attaches it to the move tree.
   *
   * @param nags the list of NAGs to attach
   * @param pointer the pointer to current and previous move nodes
   * @param root the root node to attach to (can be null for variations)
   * @return a new empty list of NAGs
   */
  private List<Nag> flushNodes(List<Nag> nags, MovePointer pointer, Node root) {
    pointer.current.setNags(nags);
    nags = new ArrayList<>();
    if (pointer.previous == null) {
      if (root != null) {
        root.setNextNode(pointer.current);
      }
    } else {
      pointer.previous.setNextNode(pointer.current);
    }
    pointer.previous = pointer.current;
    return nags;
  }

  private static class MovePointer {
    MoveNode previous;
    MoveNode current;
  }
}
