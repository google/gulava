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
package gulava.util;

import static gulava.Goals.conj;
import static gulava.Goals.same;

import gulava.Cons;
import gulava.Var;
import gulava.testing.LogicAsserter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class InsertionSortTest {
  private final Var A = new Var();

  @Test
  public void sortEmpty() {
    new LogicAsserter()
        .stream(
            InsertionSort.O.sorted(null, null))
        .workUnits(2)
        .startSubst()
        .test();
  }

  @Test
  public void sortSingle() {
    new LogicAsserter()
        .stream(
            InsertionSort.O.sorted(
                Cons.s(A), Cons.s(Count.fromInt(2))))
        .addRequestedVar(A)
        .workUnits(3)
        .startSubst()
        .put(A, Count.fromInt(2))
        .test();
  }

  @Test
  public void uninsert() {
    new LogicAsserter()
        .stream(
            InsertionSort.O.insert(
                Count.fromInt(10),
                A,
                Cons.s(
                    Count.fromInt(1),
                    Count.fromInt(10),
                    Count.fromInt(20))))
        .addRequestedVar(A)
        .workUnits(4)
        .startSubst()
        .put(A,
            Cons.s(
                Count.fromInt(1),
                Count.fromInt(20)))
        .test();
  }

  @Test
  public void sort() {
    new LogicAsserter()
        .stream(
            InsertionSort.O.sorted(
                Cons.s(
                    Count.fromInt(24),
                    Count.fromInt(2),
                    Count.fromInt(1),
                    null,
                    Count.fromInt(20)),
                A))
        .addRequestedVar(A)
        .workUnits(14)
        .startSubst()
        .put(A,
            Cons.s(
                null,
                Count.fromInt(1),
                Count.fromInt(2),
                Count.fromInt(20),
                Count.fromInt(24)))
        .test();
  }

  @Test
  public void failureWhenResultMissingSmallestElement() {
    new LogicAsserter()
        .stream(
            InsertionSort.O.sorted(
                Cons.s(
                    Count.fromInt(24),
                    Count.fromInt(2),
                    Count.fromInt(1),
                    null,
                    Count.fromInt(20)),
                Cons.of(Count.fromInt(1), A)))
        .addRequestedVar(A)
        .workUnits(9)
        // No substitutions
        .test();
  }

  @Test
  public void failureWhenResultMissingSecondSmallestElement() {
    new LogicAsserter()
        .stream(
            InsertionSort.O.sorted(
                Cons.s(
                    Count.fromInt(24),
                    Count.fromInt(2),
                    Count.fromInt(1),
                    null,
                    Count.fromInt(20)),
                Cons.of(null, Cons.of(Count.fromInt(2), A))))
        .addRequestedVar(A)
        .workUnits(10)
        // No substitutions
        .test();
  }
}
