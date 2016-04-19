/*
 *  Copyright (c) 2016 Dmitry Neverov and Google
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

import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.annotation.MakePredicates;
import gulava.testing.LogicAsserter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DelayedGoalTest {
  private final Var X = new Var();

  private static final Predicates CLAUSES =
      new MakePredicates_DelayedGoalTest_Predicates();

  private static Goal delayBy(int x, Goal g) {
    while (x-- > 0) {
      g = new DelayedGoal(g);
    }
    return g;
  }

  @MakePredicates
  public abstract static class Predicates {
    public abstract Goal member(int delayBy, Object x, Object list);

    final Goal member_found(int delayBy, Object x, Cons<?, ?> list) {
      return same(x, list.car());
    }

    final Goal member_iterate(int delayBy, Object x, Cons<?, ?> list) {
      return delayBy(delayBy, member(delayBy, x, list.cdr()));
    }
  }

  @Test
  public void delayedGoalsInDisj() {
    new LogicAsserter()
        .stream(
            disj(
                CLAUSES.member(0, X, Cons.s(1, 2, 3, 4, 5, 6)),
                CLAUSES.member(1, X, Cons.s(7, 8, 9, 10)),
                CLAUSES.member(1, X, Cons.s(11, 12, 13, 14))))
        // Items in undelayed predicate are all enumerated first.
        .startSubst()
        .put(X, 1)
        .startSubst()
        .put(X, 2)
        .startSubst()
        .put(X, 3)
        .startSubst()
        .put(X, 4)
        .startSubst()
        .put(X, 5)
        .startSubst()
        .put(X, 6)
        // The rest are interleaved fairly because they are delayed the same.
        .startSubst()
        .put(X, 7)
        .startSubst()
        .put(X, 11)
        .startSubst()
        .put(X, 8)
        .startSubst()
        .put(X, 12)
        .startSubst()
        .put(X, 9)
        .startSubst()
        .put(X, 13)
        .startSubst()
        .put(X, 10)
        .startSubst()
        .put(X, 14)
        .addRequestedVar(X)
        .test();
  }
}
