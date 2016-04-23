/*
 *  Copyright (c) 2016 The Gulava Authors
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

import gulava.testing.AssertingWriter;
import gulava.testing.LogicAsserter;
import gulava.testing.RecordsCallGoal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CachedGoalTest {
  static final Var X = new Var();
  static final Var Y = new Var();

  @Test
  public void disjIsNotReevaluated() {
    Goal cached = new CachedGoal(disj(same(X, 42), same(Y, 24)));

    new LogicAsserter()
        .stream(conj(cached, cached))
        .addRequestedVar(X, Y)
        .startSubst()
        .put(X, 42)
        .startSubst()
        .put(Y, 24)
        .test();
  }

  @Test
  public void multipleCachedGoals() {
    Goal g1 = new CachedGoal(same(X, 44));
    Goal g2 = new CachedGoal(same(Y, 99));

    new LogicAsserter()
        .stream(conj(g1, g2))
        .addRequestedVar(X, Y)
        .startSubst()
        .put(X, 44)
        .put(Y, 99)
        .test();
  }

  @Test
  public void runNotInvokedAgain() {
    StringBuilder callReport = new StringBuilder();
    RecordsCallGoal callOnce = new RecordsCallGoal(callReport, "a");
    CachedGoal goal = new CachedGoal(callOnce);

    new LogicAsserter()
        .stream(conj(goal, goal))
        .startSubst()
        .test();

    Assert.assertEquals("a", callReport.toString());
  }

  @Test
  public void prereqFail() {
    CachedGoal g1 = new CachedGoal(same(X, 44));
    Goal g2 = new CachedGoal(g1, same(Y, 100));
    new LogicAsserter()
        .stream(g2)
        .test();
  }

  @Test
  public void prereqSucceed() {
    CachedGoal g1 = new CachedGoal(same(X, 44));
    Goal g2 = new CachedGoal(g1, same(Y, 100));
    new LogicAsserter()
        .stream(conj(g1, g2))
        .addRequestedVar(X, Y)
        .startSubst()
        .put(X, 44)
        .put(Y, 100)
        .test();
  }

  @Test
  public void dumpIncludesIdentityHashCode() throws Exception {
    Goal subGoal = same(X, 42);
    Goal goal = new CachedGoal(subGoal);

    AssertingWriter writer = new AssertingWriter();
    new Dumper(/*indentation=*/0, writer)
        .dump(goal);

    // Each CachedGoal object is unequal with other instances, since it only caches itself, not the
    // delegate goal. So we don't want two CachedGoals with the same delegate Goal to appear the
    // same in dumps. identityHashCode does not guarantee unique return values for every Object, but
    // it should be good enough in practice.
    writer.assertLines(
        "CachedGoal@" + Integer.toString(System.identityHashCode(goal), 36),
        "  " + subGoal);
  }
}
