/*
 *  Copyright (c) 2015 The Gulava Authors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package gulava;

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
   * in this stream. For each solution in this stream, {@code goal} may return any number of other
   * solutions, including none.
   */
  Stream bind(Goal goal);

  /**
   * Performs one "step" and returns the resulting solution, if any, and the maybe more-realized,
   * further-advanced stream. Returns null if there are no more solutions and the streams are fully
   * realized.
   */
  SolveStep solve();
}
