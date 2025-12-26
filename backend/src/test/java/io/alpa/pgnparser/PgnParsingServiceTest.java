package io.alpa.pgnparser;

import static org.assertj.core.api.Assertions.assertThat;

import io.alpa.pgnparser.model.ChessGame;
import io.alpa.pgnparser.service.PgnParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PgnParsingServiceTest {

  private PgnParsingService service;

  @BeforeEach
  void setUp() {
    service = new PgnParsingService();
  }

  @Test
  @DisplayName("Should parse a simple PGN game")
  void testParseSimpleGame() {
    String pgn = "[Event \"Casual Game\"] 1. e4 e5 2. Nf3 Nc6 1-0";

    // When
    ChessGame game = service.pgnToChessGame(pgn);

    // Then
    assertThat(game.getTags().get("Event")).isEqualTo("Casual Game");
    assertThat(game.getResult().getCode()).isEqualTo("1-0");
  }

  @Test
  @DisplayName("Should handle missing tags gracefully")
  void testMissingTags() {
    String pgn = "1. e4 e5 1-0";

    // When
    ChessGame game = service.pgnToChessGame(pgn);

    // Then
    assertThat(game.getTags()).isEmpty();
    assertThat(game.getResult().getCode()).isEqualTo("1-0");
  }

  @Test
  @DisplayName("Should parse PGN with comments and variations")
  void testCommentsAndVariations() {
    String pgn = "[Event \"Test\"] 1. e4 {Best by test} (1. d4 d5) e5 1-0";

    // When
    ChessGame game = service.pgnToChessGame(pgn);

    // Then
    assertThat(game.getTags().get("Event")).isEqualTo("Test");
    assertThat(game.getResult().getCode()).isEqualTo("1-0");
    // Further assertions can be added for comments/variations structure
  }
}
