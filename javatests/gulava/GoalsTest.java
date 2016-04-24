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

import static gulava.Goals.conj;
import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.testing.LogicAsserter;
import gulava.testing.RecordsCallGoal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class GoalsTest {
  private static final Var X = new Var();
  private static final Var Y = new Var();

  @Test
  public void intersperseRepeated() {
    new LogicAsserter()
        .stream(disj(new RepeatedGoal(same(X, 5)), new RepeatedGoal(same(X, 6))))
        .workUnits(11)
        .finishes(false)
        .addRequestedVar(X)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .test();
  }

  @Test
  public void repeatInterspersed() {
    new LogicAsserter()
        .stream(new RepeatedGoal(disj(same(X, 5), same(X, 6))))
        .workUnits(11)
        .finishes(false)
        .addRequestedVar(X)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .startSubst().put(X, 5)
        .startSubst().put(X, 6)
        .test();
  }

  @Test
  public void unifyTwoVars() {
    new LogicAsserter()
        .stream(conj(same(X, Y), same(X, 5)))
        .addRequestedVar(X, Y)
        .workUnits(1)
        .startSubst().put(X, Y).put(Y, 5)
        .test();
  }

  @Test
  public void unifyVarWithSelf() {
    new LogicAsserter()
        .stream(same(X, X))
        .workUnits(1)
        .startSubst()
        .test();
  }

  @Test
  public void unitGoalTest() {
    new LogicAsserter()
        .stream(Goals.UNIT)
        .workUnits(1)
        .startSubst()
        .test();
  }

  @Test
  public void repeatUnitGoalTest() {
    new LogicAsserter()
        .stream(new RepeatedGoal(Goals.UNIT))
        .finishes(false)
        .workUnits(5)
        .startSubst()
        .startSubst()
        .startSubst()
        .test();
  }

  @Test
  public void conjingTwoDelayedDivergingGoalsDoesNotRequireDelaying() {
    List<Map<Object, Object>> substs = new LogicAsserter()
        .stream(
            disj(
                conj(
                    new RepeatedGoal(same(new Var(), 10)),
                    new RepeatedGoal(same(new Var(), 15))),
                same(X, 5)))
        .addRequestedVar(X)
        .finishes(false)
        .workUnits(499)
        .actualSubsts();

    Set<Object> xValues = new HashSet<>();
    for (Map<Object, Object> subst : substs) {
      xValues.add(subst.get(X));
    }
    xValues.remove(null);

    Assert.assertEquals(Collections.singleton(5), xValues);
  }

  @Test
  public void disjingTwoDelayedDivergingGoalsDoesNotRequireDelaying() {
    List<Map<Object, Object>> substs = new LogicAsserter()
        .stream(
            disj(
                disj(
                    new RepeatedGoal(same(new Var(), 10)),
                    new RepeatedGoal(same(new Var(), 15))),
                same(X, 5)))
        .addRequestedVar(X)
        .finishes(false)
        .workUnits(499)
        .actualSubsts();

    Set<Object> xValues = new HashSet<>();
    for (Map<Object, Object> subst : substs) {
      xValues.add(subst.get(X));
    }
    xValues.remove(null);

    Assert.assertEquals(Collections.singleton(5), xValues);
  }

  final static class ThrowingGoal implements Goal {
    @Override
    public Stream run(Subst s) {
      throw new AssertionError("Should not be called");
    }
  }

  @Test
  public void shortCircuitConj() {
    new LogicAsserter()
        .stream(
            conj(
                Cons.O.order(Cons.s(X), Cons.s(10, 20)),
                Cons.O.order(Cons.s(X), Cons.s(5, 15)),
                new ThrowingGoal()))
        .test();
  }

  @Test
  public void interleaveShortCircuitFirstConjIsLonger() {
    new LogicAsserter()
        .stream(
            conj(
                Cons.O.order(Cons.s(X), Cons.s(10, 20)),
                Cons.O.order(Cons.s(Y), Cons.s(15, 25)),
                new ThrowingGoal())
            .interleave(
                Cons.O.order(Cons.s(X), Cons.s(5, 15)),
                same(Y, 15)))
        .test();
  }

  @Test
  public void interleaveShortCircuitSecondConjIsLonger() {
    new LogicAsserter()
        .stream(
            conj(
                Cons.O.order(Cons.s(X), Cons.s(5, 15)),
                same(Y, 15))
            .interleave(
                Cons.O.order(Cons.s(X), Cons.s(10, 20)),
                Cons.O.order(Cons.s(Y), Cons.s(15, 25)),
                new ThrowingGoal()))
        .test();
  }

  @Test
  public void interleaveCorrectOrder() {
    StringBuilder callReport = new StringBuilder();

    new LogicAsserter()
        .stream(
            conj(
                new RecordsCallGoal(callReport, "a"),
                Cons.O.order(Cons.s(X), Cons.s(5, 15)),
                new RecordsCallGoal(callReport, "b"),
                same(Y, 15))
            .interleave(
                new RecordsCallGoal(callReport, "A"),
                new RecordsCallGoal(callReport, "B"),
                new RecordsCallGoal(callReport, "C"),
                same(X, Y)))
        .addRequestedVar(X)
        .startSubst()
        .put(X, 15)
        .test();

    // B, b, and C all appear twice because they are after the "order" goal which returns two
    // substitutions. DisjGoals interleave the results of their subgoals, so the duplicated letters
    // appear consecutively - since they are run in parallel - rather than separated.
    Assert.assertEquals("aABBbbCC", callReport.toString());
  }

  @Test
  public void unitReturnsEmptyStreamRepeatedlyAfterSubst() {
    Stream solution = Goals.UNIT.run(Subst.EMPTY);
    Assert.assertSame(EmptyStream.INSTANCE, solution.rest());
    Assert.assertSame(EmptyStream.INSTANCE, solution.rest().rest());
    Assert.assertSame(EmptyStream.INSTANCE, solution.rest().rest().rest());
    Assert.assertSame(EmptyStream.INSTANCE, solution.rest().rest().rest().rest());
  }
}
