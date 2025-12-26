package io.alpa.pgnparser;

import static io.alpa.pgnparser.util.TestUtil.createMoveNode;
import static org.assertj.core.api.Assertions.assertThat;

import io.alpa.pgnparser.enums.Nag;
import io.alpa.pgnparser.model.ChessGame;
import io.alpa.pgnparser.model.MoveNode;
import io.alpa.pgnparser.service.PgnConversionService;
import io.alpa.pgnparser.util.TestUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PgnConversionServiceTest {

  private PgnConversionService pgnConversionService;
  private ChessGame testGame;
  private ChessGame emptyGame;

  @BeforeEach
  void setUp() {
    pgnConversionService = new PgnConversionService();
    testGame = TestUtil.createTestChessGame();
    emptyGame = TestUtil.createEmptyTestChessGame();
  }

  @Test
  @DisplayName("Should extract mainline moves from a game with multiple moves")
  void testGetMainline_withMultipleMoves() {
    // When
    List<MoveNode> mainline = pgnConversionService.getMainline(testGame);

    // Then
    List<String> expectedMoves =
        List.of("e4", "c5", "Nf3", "d6", "d4", "cxd4", "Nxd4", "Nf6", "Nc3");

    assertThat(mainline).isNotNull();
    assertThat(expectedMoves).hasSameSizeAs(mainline);
    List<String> actualMoves = mainline.stream().map(MoveNode::getSan).toList();
    assertThat(expectedMoves).isEqualTo(actualMoves);
  }

  @Test
  @DisplayName("Should return empty list when extracting mainline from an empty game")
  void testGetMainline_emptyGame() {
    // When
    List<MoveNode> mainline = pgnConversionService.getMainline(emptyGame);

    // Then
    assertThat(mainline).isNotNull().isEmpty();
  }

  @Test
  @DisplayName("Should convert mainline to PGN string format for a game with multiple moves")
  void testGetMainlineAsString_withMultipleMoves() {
    // When
    String actualMainline = pgnConversionService.getMainlineAsString(testGame);

    // Then
    String expectedMainline =
        """
        [Event "Variation 1"]
        [Annotator "PGN Parser for Java"]

        1. e4 $3 {1.e4 - Best by Test} c5 {The Sicilian Defense} 2. Nf3 \
        d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 1-0""";

    assertThat(expectedMainline).isEqualTo(actualMainline);
  }

  @Test
  @DisplayName("Should convert mainline to PGN string format for an empty game")
  void testGetMainlineAsString_emptyGame() {
    // When
    String actualMainline = pgnConversionService.getMainlineAsString(emptyGame);

    // Then
    String expectedMainline =
        """
        [Event "Variation 1"]
        [Annotator "PGN Parser for Java"]""";

    assertThat(expectedMainline).isEqualTo(actualMainline);
  }

  @Test
  @DisplayName("Should convert all variations to PGN string format")
  void testGetVariationsAsStrings() {
    // When
    List<String> variations =
        new ArrayList<>(pgnConversionService.getVariationsAsStrings(testGame));

    System.out.println("Class = " + variations.getClass());
    System.out.println("Loader = " + variations.getClass().getClassLoader());

    // Then
    assertThat(variations)
        .isNotNull()
        .hasSize(5)
        .first()
        .isEqualTo(
            """
            [Event "Variation 1"]
            [Annotator "PGN Parser for Java"]

            1. e4 $3 {1.e4 - Best by Test} d4 2. cxd4 \
            Qxd4 {Missing the chance to go into the Morra Gambit.} 1-0""");
  }

  @Test
  @DisplayName("Should correctly verify the structure of the variation tree")
  void testVariationTreeStructure() {
    // Verify root node
    assertThat(testGame.getRootNode()).isNotNull();
    assertThat(testGame.getRootNode().getComment())
        .isEqualTo("This is a test game with variations");

    // Verify the first ply
    MoveNode firstPly = testGame.getRootNode().getNextNode();
    MoveNode expectedFirstPly =
        createMoveNode("e4", List.of(Nag.VERY_GOOD_MOVE), 1, "1.e4 - Best by Test");
    assertThat(firstPly).isNotNull();
    assertThat(firstPly)
        .usingRecursiveComparison()
        .ignoringFields("nextNode")
        .isEqualTo(expectedFirstPly);

    // Verify the second ply
    MoveNode secondPly = firstPly.getNextNode();
    MoveNode expectedSecondPly = createMoveNode("c5", null, 1, "The Sicilian Defense");
    assertThat(secondPly).isNotNull();
    assertThat(secondPly)
        .usingRecursiveComparison()
        .ignoringFields("nextNode", "variationNodes")
        .isEqualTo(expectedSecondPly);

    // Verify variation at the second ply
    assertThat(secondPly.getVariationNodes()).isNotNull().hasSize(1);
    MoveNode var1Ply1 = secondPly.getVariationNodes().get(0);
    MoveNode expectedVar1Ply1 = createMoveNode("d4", null, 2, null);
    assertThat(var1Ply1)
        .usingRecursiveComparison()
        .ignoringFields("nextNode", "variationNodes")
        .isEqualTo(expectedVar1Ply1);

    // Verify the fourth ply
    MoveNode fourthPly = secondPly.getNextNode().getNextNode();
    MoveNode expectedFourthPly = createMoveNode("d6", null, 2, null);
    assertThat(fourthPly).isNotNull();
    assertThat(fourthPly)
        .usingRecursiveComparison()
        .ignoringFields("nextNode", "variationNodes")
        .isEqualTo(expectedFourthPly);

    // Verify variation at the fourth ply
    assertThat(fourthPly.getVariationNodes()).isNotNull().hasSize(1);
    MoveNode var2Ply1 = fourthPly.getVariationNodes().get(0);
    MoveNode expectedVar2Ply1 = createMoveNode("Bc4", null, 3, "Italian-style approach");
    assertThat(var2Ply1)
        .usingRecursiveComparison()
        .ignoringFields("nextNode", "variationNodes")
        .isEqualTo(expectedVar2Ply1);

    // Verify variation at sixth move
    MoveNode sixthPly = fourthPly.getNextNode().getNextNode();
    assertThat(sixthPly.getVariationNodes()).isNotNull().hasSize(1);
    MoveNode var3Ply1 = sixthPly.getVariationNodes().get(0);
    MoveNode expectedVar3Ply1 = createMoveNode("Qxd4", null, 4, null);
    assertThat(var3Ply1)
        .usingRecursiveComparison()
        .ignoringFields("nextNode", "variationNodes")
        .isEqualTo(expectedVar3Ply1);

    // Verify sub-variation
    MoveNode var3Ply2 = var3Ply1.getNextNode();
    assertThat(var3Ply2.getVariationNodes()).isNotNull().hasSize(1);
    MoveNode subVar3Ply1 = var3Ply2.getVariationNodes().get(0);
    MoveNode expectedSubVar3Ply1 = createMoveNode("Qa4", List.of(Nag.GOOD_MOVE), 5, null);
    assertThat(subVar3Ply1)
        .usingRecursiveComparison()
        .ignoringFields("nextNode", "variationNodes")
        .isEqualTo(expectedSubVar3Ply1);
  }
}
