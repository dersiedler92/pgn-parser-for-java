package io.alpa.pgnparser.service;

import io.alpa.pgnparser.enums.Color;
import io.alpa.pgnparser.enums.Nag;
import io.alpa.pgnparser.model.ChessGame;
import io.alpa.pgnparser.model.MoveNode;
import java.util.*;
import org.springframework.stereotype.Service;

/** Service for converting ChessGame objects to various PGN formats. */
@Service
public class PgnConversionService {

  /**
   * Returns the mainline moves of a chess game as a list of MoveNode objects.
   *
   * @param chessGame the chess game to extract the mainline from
   * @return list of mainline moves
   */
  public List<MoveNode> getMainline(ChessGame chessGame) {
    List<MoveNode> mainline = new ArrayList<>();
    MoveNode currentMove = chessGame.getRootNode().getNextNode();

    if (currentMove == null) {
      return new ArrayList<>();
    }

    mainline.add(currentMove);

    while (currentMove.getNextNode() != null) {
      currentMove = currentMove.getNextNode();
      mainline.add(currentMove);
    }

    return mainline;
  }

  /**
   * Returns the mainline moves of a chess game as a PGN string.
   *
   * @param chessGame the chess game to extract the mainline from
   * @return PGN string of the mainline
   */
  public String getMainlineAsString(ChessGame chessGame) {
    List<MoveNode> mainline = getMainline(chessGame);

    boolean firstMoveOfLine = true;
    boolean isWhiteMove = chessGame.getRootNode().getSideToMove().equals(Color.WHITE);

    return buildPgnString(mainline, firstMoveOfLine, isWhiteMove, chessGame, new HashSet<>(), 0);
  }

  /**
   * Groups and sorts all variations in the chess game.
   *
   * @param chessGame the chess game
   * @return list of grouped and sorted variations
   */
  private List<List<MoveNode>> getGroupedSortedVariations(ChessGame chessGame) {
    List<List<MoveNode>> allVariations = new ArrayList<>();
    List<MoveNode> mainline = getMainline(chessGame);
    traverseSorted(mainline, new ArrayList<>(), allVariations);
    return allVariations;
  }

  /**
   * Traverses the move tree and collects all variations in sorted order.
   *
   * @param segment current segment of moves
   * @param pathSoFar moves collected so far
   * @param allVariations list to collect all variations
   */
  private void traverseSorted(
      List<MoveNode> segment, List<MoveNode> pathSoFar, List<List<MoveNode>> allVariations) {
    List<MoveNode> currentPath = new ArrayList<>(pathSoFar);

    for (MoveNode move : segment) {
      currentPath.add(move);

      if (move.getVariationNodes() != null && !move.getVariationNodes().isEmpty()) {
        List<MoveNode> sortedVariations = new ArrayList<>(move.getVariationNodes());
        sortedVariations.sort(Comparator.comparing(MoveNode::getSan));

        for (MoveNode varRoot : sortedVariations) {
          List<MoveNode> varMainline = buildMainlineFrom(varRoot);
          traverseSorted(
              varMainline,
              new ArrayList<>(currentPath.subList(0, currentPath.size() - 1)),
              allVariations);
        }
      }
    }

    if (!currentPath.isEmpty()) {
      allVariations.add(currentPath);
    }
  }

  /**
   * Builds the mainline starting from a given move node.
   *
   * @param root the starting move node
   * @return list of mainline moves
   */
  private List<MoveNode> buildMainlineFrom(MoveNode root) {
    List<MoveNode> mainline = new ArrayList<>();
    MoveNode current = root;
    while (current != null) {
      mainline.add(current);
      current = current.getNextNode();
    }
    return mainline;
  }

  /**
   * Returns all variations of the chess game as PGN strings.
   *
   * @param chessGame the chess game
   * @return list of PGN strings for each variation
   */
  public List<String> getVariationsAsStrings(ChessGame chessGame) {
    List<String> variationsAsStrings = new ArrayList<>();
    List<List<MoveNode>> variations = getGroupedSortedVariations(chessGame);
    Set<MoveNode> usedCommentsTracker = new HashSet<>();

    for (int i = 0; i < variations.size(); i++) {
      boolean firstMoveOfLine = true;
      boolean isWhiteMove = chessGame.getRootNode().getSideToMove() == Color.WHITE;
      variationsAsStrings.add(
          buildPgnString(
              variations.get(i), firstMoveOfLine, isWhiteMove, chessGame, usedCommentsTracker, i));
    }
    return variationsAsStrings;
  }

  /**
   * Builds a PGN string for a given variation.
   *
   * @param variation the list of moves in the variation
   * @param firstMoveOfLine whether this is the first move of the line
   * @param isWhiteMove whether the move is by white
   * @param chessGame the chess game
   * @param usedComments set of moves with comments already used
   * @param index the variation index
   * @return PGN string for the variation
   */
  private String buildPgnString(
      List<MoveNode> variation,
      boolean firstMoveOfLine,
      boolean isWhiteMove,
      ChessGame chessGame,
      Set<MoveNode> usedComments,
      int index) {
    StringBuilder sb = new StringBuilder();

    sb.append("[Event \"Variation ").append(index + 1).append("\"]\n");
    sb.append("[Annotator \"PGN Parser for Java\"]\n\n");

    for (MoveNode moveNode : variation) {
      if (firstMoveOfLine) {
        if (isWhiteMove) {
          sb.append(moveNode.getMoveNumber()).append(". ").append(moveNode.getSan());
          isWhiteMove = false;
        } else {
          sb.append(moveNode.getMoveNumber()).append("...").append(moveNode.getSan());
          isWhiteMove = true;
        }
        firstMoveOfLine = false;
      } else {
        if (isWhiteMove) {
          sb.append(moveNode.getMoveNumber()).append(". ").append(moveNode.getSan());
          isWhiteMove = false;
        } else {
          sb.append(moveNode.getSan());
          isWhiteMove = true;
        }
      }

      if (moveNode.getNags() != null && !moveNode.getNags().isEmpty()) {
        for (Nag nag : moveNode.getNags()) {
          sb.append(" $").append(nag.getCode());
        }
      }

      if (moveNode.getComment() != null
          && !moveNode.getComment().isBlank()
          && !usedComments.contains(moveNode)) {
        String cleanComment = moveNode.getComment().replaceAll("(^[{}]+)|([{}]+$)", "");
        sb.append(" {").append(cleanComment).append("} ");
        usedComments.add(moveNode);
      }

      if (!sb.toString().endsWith(" ")) {
        sb.append(" ");
      }
    }

    if (chessGame.getResult() != null) {
      sb.append(chessGame.getResult().getCode());
    }

    return sb.toString().trim();
  }

  /**
   * Returns a combined PGN string for all variations in the chess game.
   *
   * @param chessGame the chess game
   * @return combined PGN string
   */
  public String getCombinedPgn(ChessGame chessGame) {
    List<String> variations = getVariationsAsStrings(chessGame);
    StringBuilder sb = new StringBuilder();

    if (chessGame.getTags().get("Event") != null) {
      sb.append("[Event \"").append(chessGame.getTags().get("Event")).append("\"]\n");
      sb.append("*").append("\n\n");
    }

    for (String variation : variations) {
      sb.append(variation).append("\n\n");
    }

    return sb.toString().trim();
  }
}
