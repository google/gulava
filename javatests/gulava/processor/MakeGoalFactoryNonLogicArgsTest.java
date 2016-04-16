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
package gulava.processor;

import gulava.Cons;
import gulava.Goal;
import gulava.Goals;
import gulava.Var;
import gulava.annotation.MakeGoalFactory;
import gulava.testing.LogicAsserter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MakeGoalFactoryNonLogicArgsTest {
  private static final Var A = new Var();
  private static final Var B = new Var();

  @MakeGoalFactory(name = "ContainsIntLiteral")
  public static class ContainsIntLiteralClauses {
    static Goal found(int x, Cons<?, ?> a) {
      return Goals.same(x, a.car());
    }

    static Goal iterate(int x, Cons<?, ?> a) {
      return ContainsIntLiteral.o(x, a.cdr());
    }
  }

  @Test
  public void solve() {
    new LogicAsserter()
        .stream(ContainsIntLiteral.o(42, Cons.s(A, B)))
        .addRequestedVar(A, B)
        .workUnits(3)
        .startSubst()
        .put(A, 42)
        .startSubst()
        .put(B, 42)
        .test();
  }
}
