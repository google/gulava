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

import musubi.testing.LogicAsserter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class OrderTest {
  private static final Var X = new Var();
  private static final Var Y = new Var();

  @Test
  public void testEmptyLists() {
    new LogicAsserter()
        .stream(Cons.O.order(Cons.list(Arrays.asList(1)), null))
        .workUnits(1)
        .test();
    new LogicAsserter()
        .stream(new DelayedGoal(Cons.O.order(Cons.list(Arrays.asList(1)), null)))
        .workUnits(2)
        .test();

    new LogicAsserter()
        .stream(Cons.O.order(null, Cons.list(Arrays.asList(1))))
        .startSubst()
        .workUnits(2)
        .test();
  }

  @Test
  public void subIsAnySize() {
    new LogicAsserter()
        .stream(Cons.O.order(X, Cons.list(Arrays.asList(1, 2, 3, 4, 5))))
        .workUnits(33)
        .addRequestedVar(X)
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 3, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 3, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 3)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 2)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 3, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 3, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 3)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(1)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 3, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 3, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 3)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(2)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(3, 4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(3, 4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(3, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(3)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(4, 5)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(4)))
        .startSubst()
        .put(X, Cons.list(Arrays.asList(5)))
        .startSubst()
        .put(X, null)
        .test();
  }

  @Test
  public void subIsSetSize() {
    new LogicAsserter()
        .stream(Cons.O.order(
            Cons.list(Arrays.asList(X, Y)),
            Cons.list(Arrays.asList(1, 2, 3, 4, 5))))
        .workUnits(11)
        .addRequestedVar(X, Y)
        .startSubst().put(X, 1).put(Y, 2)
        .startSubst().put(X, 1).put(Y, 3)
        .startSubst().put(X, 1).put(Y, 4)
        .startSubst().put(X, 1).put(Y, 5)
        .startSubst().put(X, 2).put(Y, 3)
        .startSubst().put(X, 2).put(Y, 4)
        .startSubst().put(X, 2).put(Y, 5)
        .startSubst().put(X, 3).put(Y, 4)
        .startSubst().put(X, 3).put(Y, 5)
        .startSubst().put(X, 4).put(Y, 5)
        .test();
  }
}
