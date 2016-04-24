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
import gulava.annotation.MakePredicates;
import gulava.testing.LogicAsserter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MakePredicatesNonLogicArgsTest {
  private static final Var A = new Var();
  private static final Var B = new Var();
  private static final Var C = new Var();

  private static final Predicates CLAUSES
      = new MakePredicates_MakePredicatesNonLogicArgsTest_Predicates();

  @MakePredicates
  public static abstract class Predicates {
    public abstract Goal containsIntLiteral(int x, Object a);

    Goal containsIntLiteral_found(int x, Cons<?, ?> a) {
      return Goals.same(x, a.car());
    }

    Goal containsIntLiteral_iterate(int x, Cons<?, ?> a) {
      return containsIntLiteral(x, a.cdr());
    }

    public abstract Goal containsGrowingString(Object a, String x);

    Goal containsGrowingString_finish(Void a, String x) {
      return Goals.UNIT;
    }

    Goal containsGrowingString_iterate(Cons<?, ?> a, String x) {
      return Goals.conj(
          Goals.same(x, a.car()),
          containsGrowingString(a.cdr(), x + x));
    }

    public abstract Goal containsGrowingByChar(Object a, Cons<String, Character> x);

    void containsGrowingByChar_finish(Void a, Cons<String, Character> x) {}

    Goal containsGrowingByChar_iterate(Cons<?, ?> a, Cons<String, Character> x) {
      return Goals.conj(
          Goals.same(a.car(), x.car()),
          containsGrowingByChar(
              a.cdr(), Cons.of(x.car() + x.cdr(), x.cdr())));
    }

    public abstract Goal isOverloadedByPassThrough(int x, Object y);

    void isOverloadedByPassThrough_finish(int x, Void y) {}

    Goal isOverloadedByPassThrough_iterate(int x, Cons<?, ?> y) {
      return Goals.conj(
          Goals.same(y.car(), "int:" + x),
          isOverloadedByPassThrough(x + 1, y.cdr()));
    }

    public abstract Goal isOverloadedByPassThrough(String x, Object y);

    void isOverloadedByPassThrough_finish(String x, Void y) {}

    Goal isOverloadedByPassThrough_iterate(String x, Cons<?, ?> y) {
      return Goals.conj(
          Goals.same(y.car(), "String:" + x),
          isOverloadedByPassThrough(x + x, y.cdr()));
    }
  }

  @Test
  public void solveWithContainsIntLiteral() {
    new LogicAsserter()
        .stream(CLAUSES.containsIntLiteral(42, Cons.s(A, B)))
        .addRequestedVar(A, B)
        .workUnits(2)
        .startSubst()
        .put(A, 42)
        .startSubst()
        .put(B, 42)
        .test();
  }

  @Test
  public void solveWithContainsGrowingString() {
    new LogicAsserter()
        .stream(CLAUSES.containsGrowingString(Cons.s(A, B, C), "abc"))
        .addRequestedVar(A, B, C)
        .startSubst()
        .put(A, "abc")
        .put(B, "abcabc")
        .put(C, "abcabcabcabc")
        .test();
  }

  @Test
  public void solveWithContainsGrowingByChar() {
    new LogicAsserter()
        .stream(CLAUSES.containsGrowingByChar(Cons.s(A, B, C), Cons.of("a", 'b')))
        .addRequestedVar(A, B, C)
        .startSubst()
        .put(A, "a")
        .put(B, "ab")
        .put(C, "abb")
        .test();
  }

  @Test
  public void isOverloadedByPassThrough() {
    new LogicAsserter()
        .stream(CLAUSES.isOverloadedByPassThrough(999, Cons.s(A, B)))
        .addRequestedVar(A, B)
        .startSubst()
        .put(A, "int:999")
        .put(B, "int:1000")
        .test();

    new LogicAsserter()
        .stream(CLAUSES.isOverloadedByPassThrough("a", Cons.s(A, B)))
        .addRequestedVar(A, B)
        .startSubst()
        .put(A, "String:a")
        .put(B, "String:aa")
        .test();
  }
}
