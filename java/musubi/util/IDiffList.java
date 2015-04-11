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

import musubi.Goal;
import musubi.ICons;
import musubi.Var;
import musubi.annotation.MakeGoalFactory;
import musubi.annotation.MakeLogicValue;

/**
 * Represents a difference list. A difference list contains two plain lists: the head and the hole.
 * The head is the start of the list, and the hole is some suffix of the head which indicates that
 * the actual list stops here. Keeping the hole allows an algorithm to bind it in O(1) time, which
 * means appending to a difference list is much faster than it would be with a regular list. It also
 * means that no matter what the hole is bound to, the original list is still the same.
 */
@MakeLogicValue(name = "DiffList")
abstract class IDiffList<HEAD, HOLE> {
  public abstract HEAD head();
  public abstract HOLE hole();

  /**
   * Returns a fresh, empty difference list. This returns a new list each time.
   */
  public static IDiffList<Var, Var> empty() {
    Var node = new Var();
    return new DiffList<>(node, node);
  }

  /**
   * Defines a goal that identifies the final element in a difference list.
   */
  @MakeGoalFactory(name = "DiffListLast")
  static class DiffListLastClauses {
    static Goal goal(Object element, IDiffList<?, ICons<?, ?>> without, IDiffList<?, ?> with) {
      return conj(
          same(without.hole().car(), element),
          same(with.head(), without.head()),
          same(with.hole(), without.hole().cdr()));
    }
  }

  /**
   * Defines a goal that converts between a difference list and a plain ({@link ICons}) list.
   * Note that this usually diverges when converting a difference list to a plain list (rather than
   * the other direction). In that case, consider using {@link DiffListFinish}.
   */
  @MakeGoalFactory(name = "DiffListAsList")
  static class AsListClauses {
    static Goal delegate(IDiffList<?, ?> diffList, Object list) {
      return DiffListAsList3.i(diffList.head(), diffList.hole(), list);
    }
  }

  /**
   * Defines a goal that converts between a difference list and a plain ({@link ICons}) list. This
   * takes the fields of the difference list as separate arguments.
   *
   * <p>TODO: Figure out if this really needs to be a separate goal from {@link DiffListAsList}.
   * Maybe with inlined goals it doesn't matter if we pattern-match on the fields of a DiffList for
   * each iteration.
   */
  @MakeGoalFactory(name = "DiffListAsList3")
  static class AsList3Clauses {
    static Goal empty(Object head, Object hole, Void list) {
      return same(head, hole);
    }

    static Goal iterate(ICons<?, ?> head, Object hole, ICons<?, ?> list) {
      return conj(
          same(head.car(), list.car()),
          DiffListAsList3.o(head.cdr(), hole, list.cdr()));
    }
  }

  /**
   * Defines a goal that converts between a difference list and a plain list. This is slightly
   * different from {@link DiffListAsList} in that it renders the difference list unavailable for
   * further appending on the end, since the {@link #hole()} value is bound to {@code null}. As a
   * result, this runs in O(1) while {@link DiffListAsList} runs in O(n) where n is the length of
   * the list.
   */
  @MakeGoalFactory(name = "DiffListFinish")
  static class FinishClauses {
    static Goal goal(IDiffList<?, ?> diffList, Object list) {
      return conj(
          same(diffList.head(), list),
          same(diffList.hole(), null));
    }
  }
}
