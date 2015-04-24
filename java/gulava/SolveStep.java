/*
 *  Copyright (c) 2015 Dmitry Neverov and Google
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

import java.io.IOException;
import java.io.Writer;

/**
 * Represents a step in solving a logic problem. A step may or may not contain a valid substitution
 * (solution), but will always have a "rest" stream. Regardless of whether this step has a
 * substitution, there may be other solutions in the rest stream.
 */
public final class SolveStep implements Dumpable, Stream {
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

  @Override
  public void dump(Dumper dumper) throws IOException {
    dumper.dump("SolveStep", subst, rest);
  }
}
