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
package gulava.util;

import static gulava.Goals.conj;
import static gulava.Goals.same;

import gulava.Cons;
import gulava.Var;
import gulava.testing.LogicAsserter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class QueueTest {
  private static final Var A = new Var();
  private static final Var B = new Var();
  private static final Var C = new Var();
  private static final Var D = new Var();
  private static final Var E = new Var();
  private static final Var F = new Var();
  private static final Var G = new Var();
  private static final Var H = new Var();
  private static final Object EMPTY = Queue.empty();
  private static final Queue<Var, DiffList<?, ?>> FINISH_QUEUE =
      Queue.of(new Var(), DiffList.of(new Var(), new Var()));

  @Test
  public void enqueue() {
    new LogicAsserter()
        .stream(
            conj(
                Queue.O.last(42, EMPTY, A),
                Queue.O.last("1011", A, B),
                Queue.O.last(false, B, C),
                Queue.O.last('a', C, FINISH_QUEUE),
                same(null, FINISH_QUEUE.contents().hole()),
                same(D, FINISH_QUEUE.contents().head()),
                same(E, FINISH_QUEUE.size())))
        .addRequestedVar(D, E)
        .workUnits(1)
        .startSubst()
        .put(D, Cons.list(Arrays.asList(42, "1011", false, 'a')))
        .put(E, Count.of(Count.of(Count.of(Count.of(null)))))
        .test();
  }

  @Test
  public void enqueueDeque() {
    new LogicAsserter()
        .stream(
            conj(
                Queue.O.last(42, EMPTY, A),
                Queue.O.last("1011", A, B),
                Queue.O.last(false, B, C),
                Queue.O.first(D, E, C),
                Queue.O.first(F, G, E),
                Queue.O.first(H, FINISH_QUEUE, G)))
        .addRequestedVar(D, F, H, FINISH_QUEUE.size())
        .workUnits(1)
        .startSubst()
        .put(D, 42)
        .put(F, "1011")
        .put(H, false)
        .put(FINISH_QUEUE.size(), null)
        .test();
  }

  @Test
  public void backwardsEnqueue_inadequateCount() {
    new LogicAsserter()
        .stream(
            conj(
                Queue.O.last(42, B, Queue.of(Count.of(null), new Var())),
                Queue.O.last(42, A, B)))
        .workUnits(0)
        .test();
  }

  @Test
  public void backwardsEnqueue_success() {
    DiffList<Cons<Var, Cons<Var, Var>>, Var> startList = DiffList.of(
        Cons.of(new Var(), Cons.of(new Var(), new Var())),
        new Var());

    new LogicAsserter()
        .stream(
            conj(
                Queue.O.last(10, A, Queue.of(Count.of(Count.of(null)), startList)),
                Queue.O.last(20, EMPTY, A),
                same(startList.head().cdr().cdr(), null),
                same(C, startList.head())))
        .workUnits(1)
        .addRequestedVar(C)
        .startSubst()
        .put(C, Cons.list(Arrays.asList(20, 10)))
        .test();
  }
}
