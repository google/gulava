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
  private static final Var C = new Var();

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
  public void solveWithContainsIntLiteral() {
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

  @MakeGoalFactory(name = "ContainsGrowingString")
  public static class ContainsGrowingStringClauses {
    static Goal finish(Void a, String x) {
      return Goals.UNIT;
    }

    static Goal iterate(Cons<?, ?> a, String x) {
      return Goals.conj(
          Goals.same(x, a.car()),
          ContainsGrowingString.d(a.cdr(), x + x));
    }
  }

  @Test
  public void solveWithContainsGrowingString() {
    new LogicAsserter()
        .stream(ContainsGrowingString.o(Cons.s(A, B, C), "abc"))
        .addRequestedVar(A, B, C)
        .startSubst()
        .put(A, "abc")
        .put(B, "abcabc")
        .put(C, "abcabcabcabc")
        .test();
  }

  @MakeGoalFactory(name = "ContainsGrowingByChar")
  public static class ContainsGrowingByCharClauses {
    static void finish(Void a, Cons<String, Character> x) {}

    static Goal iterate(Cons<?, ?> a, Cons<String, Character> x) {
      return Goals.conj(
          Goals.same(a.car(), x.car()),
          ContainsGrowingByChar.d(
              a.cdr(), Cons.of(x.car() + x.cdr(), x.cdr())));
    }
  }

  @Test
  public void solveWithContainsGrowingByChar() {
    new LogicAsserter()
        .stream(ContainsGrowingByChar.o(Cons.s(A, B, C), Cons.of("a", 'b')))
        .addRequestedVar(A, B, C)
        .startSubst()
        .put(A, "a")
        .put(B, "ab")
        .put(C, "abb")
        .test();
  }
}
