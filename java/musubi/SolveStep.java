package musubi;

/**
 * Represents a step in solving a logic problem. A step may or may not contain a valid substitution
 * (solution), but will always have a "rest" stream. Regardless of whether this step has a
 * substitution, there may be other solutions in the rest stream.
 */
public final class SolveStep implements Stream {
  private final Subst subst;
  private final Stream rest;

  SolveStep(Subst subst, Stream rest) {
    if (rest == null) {
      rest = EmptyStream.INSTANCE;
    }
    this.subst = subst;
    this.rest = rest;
  }

  /**
   * A solution that was found in this step, or {@code null} if none was found.
   */
  public Subst subst() {
    return subst;
  }

  /**
   * A stream which can be used to get the rest of the solutions. This can be an empty stream.
   */
  public Stream rest() {
    return rest;
  }

  @Override
  public Stream mplus(Stream s2) {
    return new SolveStep(subst, rest.mplus(s2));
  }

  @Override
  public Stream bind(Goal goal) {
    return goal.run(subst).mplus(rest.bind(goal));
  }

  @Override
  public SolveStep solve() {
    return this;
  }
}
