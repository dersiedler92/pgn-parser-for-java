package io.alpa.pgnparser.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Nag {
  NULL_ANNOTATION(0, null),
  GOOD_MOVE(1, "!"),
  POOR_MOVE(2, "?"),
  VERY_GOOD_MOVE(3, "!!"),
  VERY_POOR_MOVE(4, "??"),
  INTERESTING_MOVE(5, "!?"),
  DUBIOUS_MOVE(6, "?!"),
  FORCED_MOVE(7, null),
  SINGULAR_MOVE(8, null),
  WORST_MOVE(9, null),
  EQUAL_POSITION(10, "="),
  EQUAL_QUIET_POSITION(11, null),
  EQUAL_ACTIVE_POSITION(12, null),
  UNCLEAR_POSITION(13, "~"),
  WHITE_SLIGHT_EDGE(14, "+="),
  BLACK_SLIGHT_EDGE(15, "=+"),
  WHITE_CLEAR_EDGE(16, "+/-"),
  BLACK_CLEAR_EDGE(17, "-/+"),
  WHITE_DECISIVE_EDGE(18, "+-"),
  BLACK_DECISIVE_EDGE(19, "-+"),
  WHITE_CRUSHING_EDGE(20, null),
  BLACK_CRUSHING_EDGE(21, null),
  WHITE_ZUGZWANG(22, null),
  BLACK_ZUGZWANG(23, null),
  WHITE_SLIGHT_SPACE_ADVANTAGE(24, null),
  BLACK_SLIGHT_SPACE_ADVANTAGE(25, null),
  WHITE_CLEAR_SPACE_ADVANTAGE(26, null),
  BLACK_CLEAR_SPACE_ADVANTAGE(27, null),
  WHITE_DECISIVE_SPACE_ADVANTAGE(28, null),
  BLACK_DECISIVE_SPACE_ADVANTAGE(29, null),
  WHITE_SLIGHT_DEVELOPMENT_ADVANTAGE(30, null),
  BLACK_SLIGHT_DEVELOPMENT_ADVANTAGE(31, null),
  WHITE_CLEAR_DEVELOPMENT_ADVANTAGE(32, null),
  BLACK_CLEAR_DEVELOPMENT_ADVANTAGE(33, null),
  WHITE_DECISIVE_DEVELOPMENT_ADVANTAGE(34, null),
  BLACK_DECISIVE_DEVELOPMENT_ADVANTAGE(35, null),
  WHITE_INITIATIVE(36, null),
  BLACK_INITIATIVE(37, null),
  WHITE_LASTING_INITIATIVE(38, null),
  BLACK_LASTING_INITIATIVE(39, null),
  WHITE_ATTACK(40, null),
  BLACK_ATTACK(41, null),
  WHITE_INSUFFICIENT_COMPENSATION(42, null),
  BLACK_INSUFFICIENT_COMPENSATION(43, null),
  WHITE_SUFFICIENT_COMPENSATION(44, null),
  BLACK_SUFFICIENT_COMPENSATION(45, null);
  // Actually many more, but realistically nothing above 44 is used.

  private final int code;
  private final String symbol;

  Nag(int code, String symbol) {
    this.code = code;
    this.symbol = symbol;
  }

  private static final Map<Integer, Nag> codeLookup =
      Arrays.stream(Nag.values()).collect(Collectors.toMap(Nag::getCode, Function.identity()));

  private static final Map<String, Nag> symbolLookup =
      Arrays.stream(Nag.values())
          .filter(n -> n.getSymbol() != null)
          .collect(Collectors.toMap(Nag::getSymbol, Function.identity()));

  public static Nag fromCode(int code) {
    return codeLookup.getOrDefault(code, NULL_ANNOTATION);
  }

  public static Nag fromSymbol(String symbol) {
    return symbolLookup.getOrDefault(symbol, NULL_ANNOTATION);
  }
}
