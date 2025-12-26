package io.alpa.pgnparser.util;

import io.alpa.pgnparser.enums.Color;
import io.alpa.pgnparser.enums.Nag;
import io.alpa.pgnparser.enums.Result;
import io.alpa.pgnparser.model.ChessGame;
import io.alpa.pgnparser.model.MoveNode;
import io.alpa.pgnparser.model.RootNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Utility class for tests providing reusable test objects. */
public class TestUtil {

  /**
   * Creates a ChessGame object with all essential fields and a broad and deep variation tree. The
   * game represents a Sicilian Defense with multiple variations.
   *
   * @return A ChessGame object for testing
   */
  public static ChessGame createTestChessGame() {
    // Create a new ChessGame
    ChessGame chessGame = new ChessGame();

    // Set up tags
    LinkedHashMap<String, String> tags = new LinkedHashMap<>();
    tags.put("Event", "Test Game");
    tags.put("Site", "Test Site");
    tags.put("Date", "2023.01.01");
    tags.put("Round", "1");
    tags.put("White", "Test White");
    tags.put("Black", "Test Black");
    tags.put("Result", "1-0");
    chessGame.setTags(tags);

    // Set up result
    chessGame.setResult(Result.WHITE_WINS);

    // Set up root node
    RootNode rootNode = new RootNode();
    rootNode.setSideToMove(Color.WHITE);
    rootNode.setComment("This is a test game with variations");
    chessGame.setRootNode(rootNode);

    // Create main line moves (1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3)
    MoveNode move1 = createMoveNode("e4", List.of(Nag.VERY_GOOD_MOVE), 1, "1.e4 - Best by Test");
    MoveNode move2 = createMoveNode("c5", null, 1, "The Sicilian Defense");
    MoveNode move3 = createMoveNode("Nf3", null, 2, null);
    MoveNode move4 = createMoveNode("d6", null, 2, null);
    MoveNode move5 = createMoveNode("d4", null, 3, null);
    MoveNode move6 = createMoveNode("cxd4", null, 3, null);
    MoveNode move7 = createMoveNode("Nxd4", null, 4, null);
    MoveNode move8 = createMoveNode("Nf6", null, 4, null);
    MoveNode move9 = createMoveNode("Nc3", null, 5, null);

    // Connect main line moves
    rootNode.setNextNode(move1);
    move1.setNextNode(move2);
    move2.setNextNode(move3);
    move3.setNextNode(move4);
    move4.setNextNode(move5);
    move5.setNextNode(move6);
    move6.setNextNode(move7);
    move7.setNextNode(move8);
    move8.setNextNode(move9);

    // Create variation 1 at move 2 (after 1. e4 c5)
    MoveNode var1Move1 = createMoveNode("d4", null, 2, null);
    MoveNode var1Move2 = createMoveNode("cxd4", null, 2, null);
    MoveNode var1Move3 =
        createMoveNode("Qxd4", null, 3, "Missing the chance to go into the Morra Gambit.");

    // Connect variation 1 moves
    var1Move1.setNextNode(var1Move2);
    var1Move2.setNextNode(var1Move3);

    // Create variation 2 at move 4 (after 1. e4 c5 2. Nf3 d6)
    MoveNode var2Move1 = createMoveNode("Bc4", null, 3, "Italian-style approach");
    MoveNode var2Move2 = createMoveNode("e6", null, 3, null);

    // Connect variation 2 moves
    var2Move1.setNextNode(var2Move2);

    // Create variation 3 at move 6 (after 1. e4 c5 2. Nf3 d6 3. d4 cxd4)
    MoveNode var3Move1 = createMoveNode("Qxd4", null, 4, null);
    MoveNode var3Move2 = createMoveNode("Nc6", null, 4, null);
    MoveNode var3Move3 = createMoveNode("Bb5", null, 5, null);

    // Connect variation 3 moves
    var3Move1.setNextNode(var3Move2);
    var3Move2.setNextNode(var3Move3);

    // Create a sub-variation of variation 3
    MoveNode subVar3Move1 = createMoveNode("Qa4", List.of(Nag.GOOD_MOVE), 5, null);
    MoveNode subVar3Move2 = createMoveNode("Bd7", null, 5, null);

    // Connect sub-variation moves
    subVar3Move1.setNextNode(subVar3Move2);

    // Add variations to their parent moves
    List<MoveNode> move2Variations = new ArrayList<>();
    move2Variations.add(var1Move1);
    move2.setVariationNodes(move2Variations);

    List<MoveNode> move4Variations = new ArrayList<>();
    move4Variations.add(var2Move1);
    move4.setVariationNodes(move4Variations);

    List<MoveNode> move6Variations = new ArrayList<>();
    move6Variations.add(var3Move1);
    move6.setVariationNodes(move6Variations);

    List<MoveNode> var3Move2Variations = new ArrayList<>();
    var3Move2Variations.add(subVar3Move1);
    var3Move2.setVariationNodes(var3Move2Variations);

    return chessGame;
  }

  public static ChessGame createEmptyTestChessGame() {
    ChessGame emptyGame = new ChessGame();
    RootNode rootNode = new RootNode();
    rootNode.setSideToMove(Color.WHITE);
    emptyGame.setRootNode(rootNode);

    return emptyGame;
  }

  /**
   * Helper method to create a MoveNode with the given parameters.
   *
   * @param san Standard Algebraic Notation of the move
   * @param nags List of Numeric Annotation Glyphs
   * @param moveNo The move number
   * @param comment Optional comment for the move
   * @return A configured MoveNode
   */
  public static MoveNode createMoveNode(String san, List<Nag> nags, int moveNo, String comment) {
    MoveNode moveNode = new MoveNode();
    moveNode.setSan(san);
    moveNode.setNags(nags);
    moveNode.setMoveNumber(moveNo);
    moveNode.setComment(comment);
    return moveNode;
  }
}
