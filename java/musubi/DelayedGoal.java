package musubi;

/**
 * A goal which evaluates equivalently to some other goal, but requires realization of an
 * {@link ImmatureStream}. This can be useful for solving stack overflow exceptions when running
 * a logic program.
 */
public final class DelayedGoal implements Goal {
  private final Goal g;

  public DelayedGoal(Goal g) {
    this.g = g;
  }

  @Override
  public Stream run(final Subst s) {
    return new ImmatureStream() {
      @Override
      protected Stream realize() {
        return g.run(s);
      }
    };
  }
}
