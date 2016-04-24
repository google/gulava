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
package gulava.lisp;

import static gulava.Cons.s;
import static gulava.Goals.conj;
import static gulava.Goals.same;
import static gulava.lisp.Lisp.O;

import gulava.Cons;
import gulava.Var;
import gulava.testing.LogicAsserter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LispTest {
  private static final Var A = new Var();
  private static final Var B = new Var();

  @Test
  public void evalQuotedTrivially() {
    new LogicAsserter()
        .stream(O.eval(s("quote", 42), new Var(), A))
        .workUnits(2)
        .addRequestedVar(A)
        .startSubst()
        .put(A, 42)
        .test();
  }

  @Test
  public void evalEnv() {
    new LogicAsserter()
        .stream(
            O.eval(
                s(
                    s("lambda", s("car", "env")),
                    s("quote", 99)),
                new Var(),
                A))
        .workUnits(8)
        .addRequestedVar(A)
        .startSubst()
        .put(A, 99)
        .test();
  }

  @Test
  public void evalKindOfInterestingLambda() {
    new LogicAsserter()
        .stream(
            O.eval(
                s(
                    s("lambda", s("cons", s("car", "env"), s("car", "env"))),
                    s("quote", 101)),
                new Var(),
                A))
        .workUnits(13)
        .addRequestedVar(A)
        .startSubst()
        .put(A, Cons.of(101, 101))
        .test();
  }

  private static final Object I_LOVE_YOU = s("I", "LOVE", "YOU");

  @Test
  public void iLoveYou_1() {
    new LogicAsserter()
        .stream(O.eval(s("quote", I_LOVE_YOU), null, A))
        .workUnits(2)
        .finishes(true)
        .addRequestedVar(A)
        .startSubst()
        .put(A, I_LOVE_YOU)
        .test();
  }

  @Test
  public void iLoveYou_2() {
    new LogicAsserter()
        .stream(O.eval(s("cdr", s("quote", Cons.of(B, I_LOVE_YOU))), null, A))
        .workUnits(4)
        .finishes(true)
        .addRequestedVar(A)
        .startSubst()
        .put(A, I_LOVE_YOU)
        .test();
  }


  @Test
  public void iLoveYou_3() {
    new LogicAsserter()
        .stream(O.eval(s(s("lambda", s(s("lambda", s("quote", I_LOVE_YOU))))), null, A))
        .workUnits(8)
        .finishes(true)
        .addRequestedVar(A)
        .startSubst()
        .put(A, I_LOVE_YOU)
        .test();
  }

  // These next two quines are kind of sad because they are self-referential

  @Test
  public void quine_1() {
    Object quine = s("cdr", s("quote", Cons.of(A, B)));
    new LogicAsserter()
        .stream(
            conj(
                same(quine, B),
                O.eval(quine, null, quine)))
        .workUnits(4)
        .startSubst()
        .test();
  }

  @Test
  public void quine_2() {
    Object quine = s("car", s("quote", Cons.of(B, A)));
    new LogicAsserter()
        .stream(
            conj(
                same(quine, B),
                O.eval(quine, null, quine)))
        .workUnits(4)
        .startSubst()
        .test();
  }

  @Test
  public void quine_3() {
    Object quine = s(s("lambda", s(s("lambda", s("quote", A)))));
    new LogicAsserter()
        .stream(
            conj(
                same(quine, A),
                O.eval(quine, null, quine)))
        .workUnits(8)
        .startSubst()
        .test();
  }

  @Test
  public void evalBuiltInFunctionReferenceFromEnv() {
    new LogicAsserter()
        .stream(
            O.eval(
                s(
                    s("lambda",
                        s(
                            s("car", "env"),
                            s("car", s("cdr", "env")))),

                    s("cons", s("quote", 43), s("quote", 42)),
                    "cdr"),
                null,
                A))
        .workUnits(19)
        .addRequestedVar(A)
        .startSubst()
        .put(A, 42)
        .test();
  }

  /**
   * A Lisp function to append two lists. Usage:
   *
   * <pre>
   * (append LEFT RIGHT)
   * </pre>
   *
   * If {@code LEFT} is not terminated with a function (rather than {@code null} like a normal
   * sequence), then that function is invoked, passing the appending of {@code LEFT'+RIGHT} as the
   * only argument, where {@code LEFT'} is {@code LEFT} with the function terminator replaced with
   * {@code null}.
   */
  private static class Append {
    // env shortcut references:
    static final Object THIS_FUNCTION = s("car", s("cdr", s("cdr", "env")));
    static final Object LEFT = s("car", s("cdr", "env"));
    static final Object RIGHT = s("car", "env");

    static final Object FN =
        s("lambda",
            s("case", LEFT,
                RIGHT,

                s("cons",
                    s("car", LEFT),
                    s(THIS_FUNCTION, s("cdr", LEFT), RIGHT)),

                s(LEFT, RIGHT)));
  }

  @Test
  public void append() {
    new LogicAsserter()
        .stream(
            O.eval(
                s(Append.FN,
                    s("cons", "car", s("cons", "car", null)),
                    s("cons", "cdr", s("cons", "cdr", null))),
                null,
                A))
        .workUnits(-1)
        .addRequestedVar(A)
        .startSubst()
        .put(A, s("car", "car", "cdr", "cdr"))
        .test();
  }

  @Test
  public void append_useFunctionBranchInCaseForm() {
    Object list = s("cons", "car", "cdr", null, "cons");
    new LogicAsserter()
        .stream(
            O.eval(
                s(Append.FN, s("lambda", s("quote", list)), s("lambda", new Var())),
                null,
                list))
        .workUnits(-1)
        .startSubst()
        .test();
  }
}
