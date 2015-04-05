package musubi.testing;

import musubi.Goal;
import musubi.SolveStep;
import musubi.Stream;
import musubi.Subst;
import musubi.Var;
import musubi.View;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object that asserts about a logic solution.
 */
public final class LogicAsserter {
  private Stream stream;
  private int expectedWorkUnits;
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
    List<Map<Var, Object>> actualSubsts = new ArrayList<>();
    boolean actualFinishes = false;
    int actualWorkUnits = 0;

    while ((actualWorkUnits++ < expectedWorkUnits) || expectedFinishes) {
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

    Assert.assertEquals(expectedWorkUnits, actualWorkUnits);
    Assert.assertEquals(expectedFinishes, actualFinishes);
    Assert.assertEquals(expectedSubsts, actualSubsts);
  }
}
