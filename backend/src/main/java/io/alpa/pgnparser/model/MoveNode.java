package io.alpa.pgnparser.model;

import io.alpa.pgnparser.enums.Nag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a move in the move tree, including SAN, variations, NAGs, and move number.
 *
 * <p>Note: {@code equals}/{@code hashCode} use identity semantics (inherited from {@link Object}).
 * Including the recursive {@code nextNode}/{@code variationNodes} fields would cause linked-list
 * traversal on every comparison and break hash-based collections (see {@code usedComments} in
 * {@code PgnConversionService}). Identity equality is appropriate here because each parsed
 * {@code MoveNode} is a unique node in the move tree.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoveNode extends Node {
  private String san;
  private List<MoveNode> variationNodes;
  private List<Nag> nags;
  private int moveNumber;
}
