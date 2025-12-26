package io.alpa.pgnparser.model;

import io.alpa.pgnparser.enums.Color;
import lombok.Getter;
import lombok.Setter;

/** Root node of the move tree, containing FEN and side to move. */
@Getter
@Setter
public class RootNode extends Node {
  private String startingFen;
  private Color sideToMove;
}
