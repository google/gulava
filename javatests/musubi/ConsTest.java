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
import static musubi.Goals.same;

import musubi.testing.LogicAsserter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class ConsTest {
  private static final Var X = new Var();
  private static final Var Y = new Var();
  private static final Var Z = new Var();

  @Test
  public void testToString() {
    Assert.assertEquals("[1]", Cons.of(1, null).toString());
    Assert.assertEquals("[1|2]", Cons.of(1, 2).toString());
    Assert.assertEquals("[1,2,3|foo]",
        Cons.of(1, Cons.of(2, Cons.of(3, "foo"))).toString());
    Assert.assertEquals("[null|42]", Cons.of(null, 42).toString());
  }

  @Test
  public void testUnifyTail() {
    new LogicAsserter()
        .stream(
            conj(
                same(X, 42),
                same(Y, Cons.list(Arrays.asList(5, 7, 9))),
                same(Z, Cons.of(X, Y))))
        .workUnits(2)
        .addRequestedVar(Z)
        .startSubst()
        .put(Z, Cons.list(Arrays.asList(42, 5, 7, 9)))
        .test();
  }
}
