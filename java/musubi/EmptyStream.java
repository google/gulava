package musubi;

/**
 * An implementation of {@link Stream} which has no solutions and no further streams to realize.
 */
public final class EmptyStream implements Stream {
  public static final Stream INSTANCE = new EmptyStream();

  private EmptyStream() {}

  @Override
  public Stream mplus(Stream s2) {
    return s2;
  }

  @Override
  public Stream bind(Goal goal) {
    return this;
  }

  @Override
  public SolveStep solve() {
    return null;
  }
}
