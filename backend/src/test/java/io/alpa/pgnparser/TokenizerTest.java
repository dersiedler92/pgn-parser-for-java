package io.alpa.pgnparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.alpa.pgnparser.enums.TokenType;
import io.alpa.pgnparser.model.Token;
import io.alpa.pgnparser.util.Tokenizer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenizerTest {

  private Tokenizer tokenizer;

  @BeforeEach
  void setUp() {
    tokenizer = new Tokenizer();
  }

  @Test
  @DisplayName("Should correctly tokenize a standard short game")
  void testStandardGame() {
    String pgn = "[Event \"Casual Game\"] 1. e4 e5 2. Nf3 Nc6 1-0";

    // When
    List<Token> tokens = tokenizer.tokenizePgn(pgn);

    // Then
    assertThat(tokens).hasSize(8);

    assertToken(tokens.get(0), TokenType.TAG, "[Event \"Casual Game\"]");
    assertToken(tokens.get(1), TokenType.MOVE_NUMBER, "1.");
    assertToken(tokens.get(2), TokenType.MOVE, "e4");
    assertToken(tokens.get(3), TokenType.MOVE, "e5");
    assertToken(tokens.get(4), TokenType.MOVE_NUMBER, "2.");
    assertToken(tokens.get(5), TokenType.MOVE, "Nf3");
    assertToken(tokens.get(6), TokenType.MOVE, "Nc6");
    assertToken(tokens.get(7), TokenType.RESULT, "1-0");
  }

  @Test
  @DisplayName("Should handle Castling (Standard and Zero notation) and Checks/Mates")
  void testSpecialMoveNotation() {
    String pgn = "1. O-O 0-0-0+ Qxd5#";

    // When
    List<Token> tokens = tokenizer.tokenizePgn(pgn);

    // Then
    assertThat(tokens).hasSize(4);

    assertThat(tokens)
        .extracting(Token::getType)
        .containsExactly(TokenType.MOVE_NUMBER, TokenType.MOVE, TokenType.MOVE, TokenType.MOVE);

    assertThat(tokens).extracting(Token::getValue).contains("O-O", "0-0-0+", "Qxd5#");
  }

  @Test
  @DisplayName("Should handle Pawn Promotions")
  void testPawnPromotions() {
    String pgn = "a8=Q d1=N+";

    // When
    List<Token> tokens = tokenizer.tokenizePgn(pgn);

    // Then
    assertThat(tokens).extracting(Token::getValue).containsExactly("a8=Q", "d1=N+");

    assertThat(tokens).extracting(Token::getType).containsOnly(TokenType.MOVE);
  }

  @Test
  @DisplayName("Should handle Comments and Variations correctly")
  void testCommentsAndVariations() {
    String pgn = "1. e4 {Best by test} (1. d4 d5) 1... c5";

    // When
    List<Token> tokens = tokenizer.tokenizePgn(pgn);

    // Then
    assertThat(tokens).hasSize(10);

    assertToken(tokens.get(2), TokenType.COMMENT, "{Best by test}");
    assertToken(tokens.get(3), TokenType.VARIATION_START, "(");
    assertToken(tokens.get(7), TokenType.VARIATION_END, ")");
    assertToken(tokens.get(8), TokenType.MOVE_NUMBER, "1...");
  }

  @Test
  @DisplayName("Should separate NAGs (annotations) from moves")
  void testNags() {
    String pgn = "e4! d5? c4?!";

    // When
    List<Token> tokens = tokenizer.tokenizePgn(pgn);

    // Then
    assertThat(tokens).hasSize(6);

    assertThat(tokens)
        .extracting(Token::getType)
        .containsExactly(
            TokenType.MOVE, TokenType.NAG,
            TokenType.MOVE, TokenType.NAG,
            TokenType.MOVE, TokenType.NAG);

    assertThat(tokens)
        .extracting(Token::getValue)
        .containsExactly("e4", "!", "d5", "?", "c4", "?!");
  }

  @Test
  @DisplayName("Should handle various game results")
  void testResults() {
    List<String> results = List.of("1-0", "0-1", "1/2-1/2", "*");

    for (String res : results) {
      // When
      List<Token> tokens = tokenizer.tokenizePgn(res);

      // Then
      assertThat(tokens)
          .singleElement()
          .satisfies(
              token -> {
                assertThat(token.getType()).isEqualTo(TokenType.RESULT);
                assertThat(token.getValue()).isEqualTo(res);
              });
    }
  }

  @Test
  @DisplayName("Should throw exception on unknown tokens")
  void testInvalidToken() {
    String pgn = "1. e4 %Invalid%";

    // When/Then
    assertThatThrownBy(() -> tokenizer.tokenizePgn(pgn))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown token")
        .hasMessageContaining("%Invalid%");
  }

  @Test
  @DisplayName("Should normalize messy whitespace")
  void testWhitespaceNormalization() {
    String pgn = "1.   e4 \n \t e5";

    // When
    List<Token> tokens = tokenizer.tokenizePgn(pgn);

    // Then
    assertThat(tokens).hasSize(3);
    assertThat(tokens).extracting(Token::getValue).containsExactly("1.", "e4", "e5");
  }

  private void assertToken(Token token, TokenType expectedType, String expectedValue) {
    assertThat(token).extracting("type", "value").containsExactly(expectedType, expectedValue);
  }
}
