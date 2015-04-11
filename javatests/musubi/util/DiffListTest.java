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
package musubi.util;

import static musubi.Goals.conj;
import static musubi.Goals.same;

import musubi.Cons;
import musubi.ICons;
import musubi.Var;
import musubi.testing.LogicAsserter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class DiffListTest {
  private static final IDiffList<Var, Var> EMPTY = IDiffList.empty();
  private static final Var A = new Var();
  private static final Var B = new Var();
  private static final Var C = new Var();

  @Test
  public void finishDisablesFurtherAppending() {
    new LogicAsserter()
        .stream(
            conj(
                DiffListLast.o(42, EMPTY, A),
                DiffListLast.o('a', A, B),
                DiffListFinish.o(B, C),
                DiffListLast.o("hello", B, new Var())))
        .workUnits(1)
        .test();
  }

  @Test
  public void finish() {
    new LogicAsserter()
        .stream(
            conj(
                DiffListLast.o(42, EMPTY, A),
                DiffListLast.o('a', A, B),
                DiffListFinish.o(B, C)))
        .workUnits(2)
        .addRequestedVar(C)
        .startSubst()
        .put(C, ICons.list(Arrays.asList(42, 'a')))
        .test();
  }

  @Test
  public void fromListEmpty() {
    new LogicAsserter()
        .stream(
            conj(
                DiffListAsList.o(A, null),
                same(A, new DiffList<>(B, C))))
        .workUnits(2)
        .addRequestedVar(B, C)
        .startSubst()
        .put(B, C)
        .test();
  }

  @Test
  public void fromList() {
    IDiffList<Cons<?, Cons<?, ?>>, ?> diffList = new DiffList<>(
        new Cons<>(new Var(), new Cons<>(new Var(), new Var())),
        new Var());
    new LogicAsserter()
        .stream(
            conj(
                DiffListAsList.o(diffList, ICons.list(Arrays.asList(42, 'a'))),
                same(diffList.head().car(), B),
                same(diffList.head().cdr().car(), C)))
        .workUnits(2)
        .addRequestedVar(B, C)
        .startSubst()
        .put(B, 42)
        .put(C, 'a')
        .test();
  }
}
