package musubi;

public final class Var {
  private static int nextC = 0;
  private final int c = nextC++;

  @Override
  public String toString() {
    return String.format("_.%x", c);
  }
}
