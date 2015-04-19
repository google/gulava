package musubi;

/**
 * A stream of substitutions, or logic solutions. A solution is a substitution that fulfills a
 * {@link Goal}.
 */
public interface Stream {
  /**
   * Combines this stream with the stream given by {@code s2}. The resulting stream will "return"
   * all solutions in this stream and in {@code s2} (with calls to {@link #solve()}). The order in
   * which the solutions are returned depends on the implementations of {@code Stream} used.
   */
  Stream mplus(Stream s2);

  /**
   * Returns a stream which contains the successful applications of {@code goal} to the solutions
   * in this stream. Vaguely speaking, the number of solutions in the resulting stream is less than
   * or equal to this stream.
   */
  Stream bind(Goal goal);

  /**
   * Performs one "step" and returns the resulting solution, if any, and the maybe more-realized,
   * further-advanced stream. Returns null if there are no more solutions and the streams are fully
   * realized.
   */
  SolveStep solve();
}
