package io.alpa.pgnparser.model;

import lombok.Getter;
import lombok.Setter;

/** Abstract base class for move tree nodes. */
@Getter
@Setter
public abstract class Node {
  protected String comment;
  protected MoveNode nextNode;
}
