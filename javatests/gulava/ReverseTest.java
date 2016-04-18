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

import gulava.testing.LogicAsserter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class ReverseTest {
  private static final Var X = new Var();
  private static final Var Y = new Var();

  @Test
  public void plainReverse() {
    new LogicAsserter()
        .stream(Cons.O.reverse(Cons.list(Arrays.asList(4, 5, 6)), X))
        .workUnits(5)
        .addRequestedVar(X)
        .startSubst()
        .put(X, Cons.list(java.util.Arrays.asList(6, 5, 4)))
        .test();
  }

  @Test
  public void noBoundVars() {
    List<Map<Var, Object>> substs = new LogicAsserter()
        .stream(Cons.O.reverse(X, Y))
        .workUnits(11)
        .finishes(false)
        .addRequestedVar(X, Y)
        .actualSubsts();

    Assert.assertEquals(6, substs.size());

    for (int i = 0; i < substs.size(); i++) {
      List<Object> xList = Cons.toList((Cons) substs.get(i).get(X));
      Assert.assertEquals(i, xList.size());
      Collections.reverse(xList);
      Assert.assertEquals(xList, Cons.toList((Cons) substs.get(i).get(Y)));
    }
  }
}
