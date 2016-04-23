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
package gulava.testing;

import gulava.Goal;
import gulava.SolveStep;
import gulava.Stream;
import gulava.Subst;
import gulava.Var;
import gulava.View;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object that asserts about a logic solution.
 */
public final class LogicAsserter {
  private Stream stream;
  private int expectedWorkUnits = -1;
  private List<Map<Var, Object>> expectedSubsts = new ArrayList<>();
  private List<Var> requestedVars = new ArrayList<>();
  private boolean expectedFinishes = true;

  /**
   * Set the stream whose results to assert on.
   */
  public LogicAsserter stream(Stream stream) {
    this.stream = stream;
    return this;
  }

  /**
   * Set the stream whose results to assert on. The stream is the result of running the goal on the
   * empty substitution.
   */
  public LogicAsserter stream(Goal goal) {
    this.stream = goal.run(Subst.EMPTY);
    return this;
  }

  /**
   * Set the number of work units, which roughly corresponds to how many times we realize or obtain
   * a sub-{@link Stream}. If we expect to finish, this should be set to the exact number of work
   * units before we realize the stream. Otherwise, this is the number of work units before we stop
   * obtaining results for the test.
   */
  public LogicAsserter workUnits(int expectedWorkUnits) {
    this.expectedWorkUnits = expectedWorkUnits;
    return this;
  }

  /**
   * Begins a new substitution map.
   */
  public LogicAsserter startSubst() {
    expectedSubsts.add(new HashMap<>());
    return this;
  }

  /**
   * Adds an entry to the current substitution map.
   */
  public LogicAsserter put(Var var, Object value) {
    expectedSubsts.get(expectedSubsts.size() - 1).put(var, value);
    return this;
  }

  /**
   * Indicates whether the stream is expected to finish. This is true by default.
   */
  public LogicAsserter finishes(boolean expectedFinishes) {
    this.expectedFinishes = expectedFinishes;
    return this;
  }

  public LogicAsserter addRequestedVar(Var... vars) {
    this.requestedVars.addAll(Arrays.asList(vars));
    return this;
  }

  /**
   * Performs the test by verifying whether the expected values set with the other methods match the
   * actual results obtained from the stream.
   */
  public void test() {
    Assert.assertEquals(expectedSubsts, execute());
  }

  private List<Map<Object, Object>> execute() {
    List<Map<Object, Object>> actualSubsts = new ArrayList<>();
    boolean actualFinishes = false;
    int actualWorkUnits = 0;

    while (expectedFinishes || (actualWorkUnits < expectedWorkUnits)) {
      actualWorkUnits += 1;
      SolveStep solve = stream.solve();
      if (solve == null) {
        actualFinishes = true;
        break;
      }
      if (solve.subst() != null) {
        actualSubsts.add(new View.Builder()
            .setSubst(solve.subst())
            .addAllRequestedVars(requestedVars)
            .build()
            .map());
      }
      stream = solve.rest();
    }

    if (expectedWorkUnits != -1) {
      Assert.assertEquals(expectedWorkUnits, actualWorkUnits);
    }
    Assert.assertEquals(expectedFinishes, actualFinishes);
    return actualSubsts;
  }

  /**
   * Rather than compare expected substitutions to actual ones (like {@link #test()} does), just
   * returns the substitutions and allows the caller to verify them. This still asserts that the
   * correct number of work units were spent and and that the stream did or did not finish according
   * to expectations.
   */
  public List<Map<Object, Object>> actualSubsts() {
    Assert.assertEquals(Collections.emptyList(), expectedSubsts);
    return execute();
  }
}
