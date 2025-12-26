package io.alpa.pgnparser.util;

import io.alpa.pgnparser.enums.TokenType;
import io.alpa.pgnparser.model.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for tokenizing PGN strings into Token objects. */
public class Tokenizer {

  /**
   * Tokenizes a PGN string into a list of Token objects.
   *
   * @param pgn the PGN string
   * @return list of tokens
   */
  public List<Token> tokenizePgn(String pgn) {
    // Normalize whitespace to avoid accidental merging
    String normalizedGame = pgn.replaceAll("\\s+", " ").trim();

    // Build a combined regex pattern from the map
    String validPatterns =
        TOKEN_PATTERNS.keySet().stream()
            .map(Pattern::pattern)
            .reduce((a, b) -> a + "|" + b)
            .orElseThrow(() -> new IllegalStateException("No patterns defined"));

    // Add non-whitespace pattern to the compiled pattern.
    Pattern pattern = Pattern.compile(validPatterns + "|\\S+");
    Matcher matcher = pattern.matcher(normalizedGame);

    List<Token> tokens = new ArrayList<>();
    while (matcher.find()) {
      String value = matcher.group().trim();
      tokens.add(classifyToken(value));
    }

    return tokens;
  }

  /**
   * Classifies a string value into a Token object based on regex patterns.
   *
   * @param value the string value
   * @return the classified Token
   */
  private Token classifyToken(String value) {
    for (Map.Entry<Pattern, TokenType> entry : TOKEN_PATTERNS.entrySet()) {
      if (entry.getKey().matcher(value).matches()) {
        return new Token(entry.getValue(), value);
      }
    }
    throw new IllegalArgumentException("Unknown token: " + value);
  }

  private static final Map<Pattern, TokenType> TOKEN_PATTERNS =
      Map.of(
          Pattern.compile("\\[[^\\]]+\\]"), TokenType.TAG,
          Pattern.compile("\\d+\\.(?:\\.\\.)?\\s*"), TokenType.MOVE_NUMBER,
          Pattern.compile("\\$\\d+|!!|\\?\\?|!\\?|\\?!|[!?=]|[+-][/\\-]?[+-]?"), TokenType.NAG,
          Pattern.compile("\\{[^}]*\\}"), TokenType.COMMENT,
          Pattern.compile("\\("), TokenType.VARIATION_START,
          Pattern.compile("\\)"), TokenType.VARIATION_END,
          Pattern.compile("O-O(-O)?[+#]?|0-0(-0)?[+#]?"), TokenType.MOVE,
          Pattern.compile("[KQRBN]?[a-h]?[1-8]?x?[a-h][1-8](=[QRBN])?[+#]?"), TokenType.MOVE,
          Pattern.compile("1-0|0-1|1/2-1/2|\\*"), TokenType.RESULT);
}
