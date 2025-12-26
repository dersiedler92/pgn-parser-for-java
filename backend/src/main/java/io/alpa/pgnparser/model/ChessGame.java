package io.alpa.pgnparser.model;

import io.alpa.pgnparser.enums.Result;
import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents a parsed chess game, including tags, moves, and result. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChessGame {
  private LinkedHashMap<String, String> tags;
  private RootNode rootNode;
  private Result result;
}
