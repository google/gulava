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

import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.testing.LogicAsserter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class AppendTest {
  private final Var X = new Var();
  private final Var Y = new Var();

  @Test
  public void onlyLastArgBound() {
    new LogicAsserter()
        .stream(Cons.O.append(X, Y, Cons.list(Arrays.asList(1, 2, 3, 4))))
        .workUnits(10)
        .startSubst()
        .put(X, null).put(Y, Cons.list(Arrays.asList(1, 2, 3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1))).put(Y, Cons.list(Arrays.asList(2, 3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2))).put(Y, Cons.list(Arrays.asList(3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 3))).put(Y, Cons.list(Arrays.asList(4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 3, 4))).put(Y, null)
        .addRequestedVar(X, Y)
        .test();
  }

  @Test
  public void divergingAppendYieldsToNextGoalInDisj() {
    List<Map<Object, Object>> substs = new LogicAsserter()
        .stream(
            disj(
                Cons.O.append(new Var(), new Var(), new Var()),
                same(X, 5)))
        .finishes(false)
        .workUnits(100)
        .addRequestedVar(X)
        .actualSubsts();

    Set<Object> xValues = new HashSet<>();
    for (Map<Object, Object> subst : substs) {
      xValues.add(subst.get(X));
    }
    xValues.remove(null);
    Assert.assertEquals(Collections.singleton(5), xValues);
  }
}
