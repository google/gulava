package musubi;

public abstract class ImmatureStream implements Stream {
  protected abstract Stream realize();

  @Override
  public final Stream mplus(final Stream s2) {
    final ImmatureStream outer = this;

    return new ImmatureStream() {
      @Override
      protected Stream realize() {
        return s2.mplus(outer.realize());
      }
    };
  }

  @Override
  public final Stream bind(final Goal goal) {
    final ImmatureStream outer = this;

    return new ImmatureStream() {
      @Override
      protected Stream realize() {
        return outer.realize().bind(goal);
      }
    };
  }

  @Override
  public final SolveStep solve() {
    return new SolveStep(null, realize());
  }
}
