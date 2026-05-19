package io.alpa.pgnparser.util;

import io.alpa.pgnparser.enums.TokenType;
import io.alpa.pgnparser.model.Token;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for tokenizing PGN strings into Token objects. */
public class Tokenizer {

  // Ordered list of (named-group, regex, TokenType). Order matters: first match wins.
  // Group names must be valid Java identifiers (letters/digits, starting with a letter).
  private static final List<TokenSpec> TOKEN_SPECS =
      List.of(
          new TokenSpec("TAG", "\\[[^\\]]+\\]", TokenType.TAG),
          new TokenSpec("MOVENUMBER", "\\d+\\.(?:\\.\\.)?", TokenType.MOVE_NUMBER),
          new TokenSpec("COMMENT", "\\{[^}]*\\}", TokenType.COMMENT),
          new TokenSpec("VARSTART", "\\(", TokenType.VARIATION_START),
          new TokenSpec("VAREND", "\\)", TokenType.VARIATION_END),
          new TokenSpec("RESULT", "1-0|0-1|1/2-1/2|\\*", TokenType.RESULT),
          new TokenSpec("CASTLE", "O-O(?:-O)?[+#]?|0-0(?:-0)?[+#]?", TokenType.MOVE),
          new TokenSpec(
              "SAN", "[KQRBN]?[a-h]?[1-8]?x?[a-h][1-8](?:=[QRBN])?[+#]?", TokenType.MOVE),
          new TokenSpec("NAG", "\\$\\d+|!!|\\?\\?|!\\?|\\?!|[!?=]|[+-][/\\-]?[+-]?", TokenType.NAG),
          // Fallback: any non-whitespace run; classified as UNKNOWN below.
          new TokenSpec("UNKNOWN", "\\S+", null));

  private static final Pattern COMBINED_PATTERN = buildCombinedPattern();

  // Preserves insertion order for first-match-wins semantics on group inspection.
  private static final Map<String, TokenType> GROUP_TO_TYPE = buildGroupTypeMap();

  private static Pattern buildCombinedPattern() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < TOKEN_SPECS.size(); i++) {
      if (i > 0) sb.append('|');
      TokenSpec s = TOKEN_SPECS.get(i);
      sb.append("(?<").append(s.groupName).append('>').append(s.regex).append(')');
    }
    return Pattern.compile(sb.toString());
  }

  private static Map<String, TokenType> buildGroupTypeMap() {
    Map<String, TokenType> m = new LinkedHashMap<>();
    for (TokenSpec s : TOKEN_SPECS) {
      m.put(s.groupName, s.type);
    }
    return m;
  }

  /**
   * Tokenizes a PGN string into a list of Token objects.
   *
   * @param pgn the PGN string
   * @return list of tokens
   */
  public List<Token> tokenizePgn(String pgn) {
    Matcher matcher = COMBINED_PATTERN.matcher(pgn);
    List<Token> tokens = new ArrayList<>();
    while (matcher.find()) {
      TokenType matchedType = null;
      String matchedValue = null;
      for (Map.Entry<String, TokenType> entry : GROUP_TO_TYPE.entrySet()) {
        String value = matcher.group(entry.getKey());
        if (value != null) {
          matchedType = entry.getValue();
          matchedValue = value;
          break;
        }
      }
      if (matchedType == null) {
        // Hit the UNKNOWN fallback group.
        throw new IllegalArgumentException("Unknown token: " + matchedValue);
      }
      tokens.add(new Token(matchedType, matchedValue));
    }
    return tokens;
  }

  private static final class TokenSpec {
    final String groupName;
    final String regex;
    final TokenType type; // null => UNKNOWN fallback

    TokenSpec(String groupName, String regex, TokenType type) {
      this.groupName = groupName;
      this.regex = regex;
      this.type = type;
    }
  }
}
