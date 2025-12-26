package io.alpa.pgnparser.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Result {
  WHITE_WINS("1-0"),
  BLACK_WINS("0-1"),
  DRAW("1/2-1/2"),
  NO_RESULT("*");

  private final String code;

  Result(String code) {
    this.code = code;
  }

  private static final Map<String, Result> lookup =
      Arrays.stream(Result.values())
          .collect(Collectors.toMap(Result::getCode, Function.identity()));

  public static Result fromCode(String code) {
    return lookup.get(code);
  }
}
