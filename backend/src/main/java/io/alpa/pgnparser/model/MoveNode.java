package io.alpa.pgnparser.model;

import io.alpa.pgnparser.enums.Nag;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents a move in the move tree, including SAN, variations, NAGs, and move number. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoveNode extends Node {
  private String san;
  private List<MoveNode> variationNodes;
  private List<Nag> nags;
  private int moveNumber;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MoveNode moveNode)) return false;
    return moveNumber == moveNode.moveNumber
        && Objects.equals(san, moveNode.san)
        && Objects.equals(comment, moveNode.comment)
        && Objects.equals(nextNode, moveNode.nextNode)
        && Objects.equals(variationNodes, moveNode.variationNodes)
        && Objects.equals(nags, moveNode.nags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(san, comment, nextNode, variationNodes, nags, moveNumber);
  }
}
