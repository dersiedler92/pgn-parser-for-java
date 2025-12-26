package io.alpa.pgnparser.model;

import io.alpa.pgnparser.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Represents a token parsed from PGN, with type and value. */
@Getter
@Setter
@AllArgsConstructor
public class Token {
  private TokenType type;
  private String value;
}
