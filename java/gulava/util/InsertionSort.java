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

import static gulava.Goals.UNIT;
import static gulava.Goals.conj;
import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.Cons;
import gulava.DelayedGoal;
import gulava.Goal;
import gulava.Var;
import gulava.annotation.MakePredicates;

@MakePredicates
public abstract class InsertionSort {
  public static final InsertionSort O = new MakePredicates_InsertionSort();

  public abstract Goal sorted(Object original, Object sorted);

  final Goal sorted_baseCase(Void original, Void sorted) {
    return UNIT;
  }

  final Goal sorted_insertNext(Cons<?, ?> original, Object sorted) {
    Var sortedRest = new Var();

    return conj(
        new DelayedGoal(sorted(original.cdr(), sortedRest)),
        insert(original.car(), sortedRest, sorted));
  }

  public abstract Goal insert(Object element, Object original, Object inserted);

  final Goal insert_intoEmpty(
      Object element, Void original, Cons<?, Void> inserted) {
    return same(element, inserted.car());
  }

  final Goal insert_atHead(
      Object element, Cons<?, ?> original, Cons<?, ?> inserted) {
    return conj(
        Count.O.less(element, original.car()),
        same(original, inserted.cdr()),
        same(element, inserted.car()));
  }

  final Goal insert_notAtHead(
      Object element, Cons<?, ?> original, Cons<?, ?> inserted) {
    return conj(
        Count.O.lessOrEqual(original.car(), element),
        same(original.car(), inserted.car()),
        new DelayedGoal(insert(element, original.cdr(), inserted.cdr())));
  }
}
