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
import gulava.Goal;
import gulava.Var;
import gulava.annotation.MakeLogicValue;
import gulava.annotation.MakePredicates;

/**
 * Represents a difference list. A difference list contains two plain lists: the head and the hole.
 * The head is the start of the list, and the hole is some suffix of the head which indicates that
 * the actual list stops here. Keeping the hole allows an algorithm to bind it in O(1) time, which
 * means appending to a difference list is much faster than it would be with a regular list. It also
 * means that no matter what the hole is bound to, the original list is still the same.
 */
@MakeLogicValue
abstract class DiffList<HEAD, HOLE> {
  public abstract HEAD head();
  public abstract HOLE hole();

  public static <HEAD, HOLE> DiffList<HEAD, HOLE> of(HEAD head, HOLE hole) {
    return new MakeLogicValue_DiffList<>(head, hole);
  }

  /**
   * Returns a fresh, empty difference list. This returns a new list each time.
   */
  public static DiffList<Var, Var> empty() {
    Var node = new Var();
    return of(node, node);
  }

  public static final Goals O = new MakePredicates_DiffList_Goals();

  /**
   * Goals related to difference lists.
   */
  @MakePredicates
  public static abstract class Goals {
    /**
     * Identifies the final element in a difference list.
     *
     * @param element the item that is last in the list
     * @param without the difference list without the element
     * @param with the difference list with the element
     */
    public abstract Goal last(Object element, Object without, Object with);

    final Goal last_impl(Object element, DiffList<?, Cons<?, ?>> without, DiffList<?, ?> with) {
      return conj(
          same(without.hole().car(), element),
          same(with.head(), without.head()),
          same(with.hole(), without.hole().cdr()));
    }

    /**
     * Identifies the first element in a difference list.
     *
     * @param element the item that is first in the list
     * @param without the difference list without the element
     * @param with the difference list with the element
     */
    public abstract Goal first(Object element, Object without, Object with);

    final Goal first_impl(Object element, DiffList<?, Cons<?, ?>> without, DiffList<?, ?> with) {
      return conj(
          same(without.hole(), with.hole()),
          same(Cons.of(element, without.head()), with.head()));
    }

    /**
     * Converts between a difference list and a plain ({@link Cons}) list. Note that this usually
     * diverges when converting a difference list to a plain list (rather than the other direction).
     * In that case, consider using {@link #finish(Object, Object)}.
     */
    public abstract Goal asList(Object diffList, Object consList);

    final Goal asList_delegate(DiffList<?, ?> diffList, Object consList) {
      return asList(diffList.head(), diffList.hole(), consList);
    }

    /**
     * Converts between a difference list and a plain ({@link Cons}) list. This takes the fields of
     * the difference list as separate arguments.
     *
     * <p>TODO: Figure out if this really needs to be a separate goal from {@link DiffListAsList}.
     * Maybe with inlined goals it doesn't matter if we pattern-match on the fields of a DiffList
     * for each iteration.
     */
    public abstract Goal asList(Object head, Object hole, Object consList);

    final Goal asList_empty(Object head, Object hole, Void consList) {
      return same(head, hole);
    }

    final Goal asList_iterate(Cons<?, ?> head, Object hole, Cons<?, ?> consList) {
      return conj(
          same(head.car(), consList.car()),
          asList(head.cdr(), hole, consList.cdr()));
    }

    /**
     * Converts between a difference list and a plain list. This is slightly
     * different from {@link DiffListAsList} in that it renders the difference list unavailable for
     * further appending on the end, since the {@link #hole()} value is bound to {@code null}. As a
     * result, this runs in O(1) while {@link DiffListAsList} runs in O(n) where n is the length of
     * the list.
     */
    public abstract Goal finish(Object diffList, Object consList);

    final Goal finish_impl(DiffList<?, ?> diffList, Object consList) {
      return conj(
          same(diffList.head(), consList),
          same(diffList.hole(), null));
    }
  }
}
