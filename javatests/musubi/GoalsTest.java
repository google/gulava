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
package musubi;

import static musubi.Goals.conj;
import static musubi.Goals.disj;
import static musubi.Goals.repeat;
import static musubi.Goals.same;

import musubi.testing.LogicAsserter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GoalsTest {
  private static final Var X = new Var();
  private static final Var Y = new Var();

  @Test
  public void intersperseRepeated() {
    new LogicAsserter()
        .stream(disj(repeat(same(X, 5)), repeat(same(X, 6))))
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
        .stream(repeat(disj(same(X, 5), same(X, 6))))
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
        .workUnits(2)
        .startSubst().put(X, Y).put(Y, 5)
        .test();
  }

  @Test
  public void unifyVarWithSelf() {
    new LogicAsserter()
        .stream(same(X, X))
        .workUnits(2)
        .startSubst()
        .test();
  }
}
