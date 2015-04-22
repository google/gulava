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
package gulava;

import static gulava.Goals.UNIT;
import static gulava.Goals.conj;
import static gulava.Goals.same;

import gulava.annotation.MakeLogicValue;
import gulava.annotation.MakePredicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * A general-purpose logic value which contains two fields not necessarily with a particular
 * meaning. This can be used, for instance, to store a sequence or a binary tree. To store a
 * sequence, the first field (car) is the start of the sequence, and the second field (cdr) is
 * either null (for singleton sequences) or is another sequence which contains the elements after
 * car. The {@link #toString()} implementation formats the output nicely when used with sequences
 * so it looks like a normal list and not a nested data structure.
 */
@MakeLogicValue
public abstract class Cons<CAR, CDR> {
  /**
   * Package-protected constructor because only {@link Cons} should subclass.
   */
  Cons() {}

  public abstract CAR car();
  public abstract CDR cdr();

  public static <CAR, CDR> Cons<CAR, CDR> of(CAR car, CDR cdr) {
    return new MakeLogicValue_Cons<>(car, cdr);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[")
        .append(car());
    Object rest = cdr();
    while (rest instanceof Cons) {
      Cons restCons = (Cons) rest;
      result.append(",")
          .append(restCons.car());
      rest = restCons.cdr();
    }
    if (rest != null) {
      result.append("|")
          .append(rest);
    }
    return result
        .append("]")
        .toString();
  }

  /**
   * Converts a list to a sequence as described in this class' Javadoc above.
   */
  public static Cons list(List<?> values) {
    Cons result = null;
    for (ListIterator<?> valueIter = values.listIterator(values.size()); valueIter.hasPrevious();) {
      result = Cons.of(valueIter.previous(), result);
    }
    return result;
  }

  /**
   * Syntactic sugar for {@code Cons.list(Arrays.asList(...))}.
   */
  public static Cons<?, ?> s(Object... o) {
    return Cons.list(Arrays.asList(o));
  }

  /**
   * Converts a sequence to a list as described in this class' Javadoc above.
   */
  public static List<Object> toList(Cons head) {
    List<Object> list = new ArrayList<>();
    while (head != null) {
      list.add(head.car());
      head = (Cons) head.cdr();
    }
    return list;
  }

  public static final Goals O = new MakePredicates_Cons_Goals();

  /** Goals related to {@link Cons} cells and sequences thereof. */
  @MakePredicates
  public static abstract class Goals {
    /**
     * Appends two sequences to create another sequence. In more general terms, identifies a
     * sequence as the concatenation of two other sequences.
     */
    public abstract Goal append(Object a, Object b, Object ab);

    final Goal append_baseCase(Void a, Object b, Object ab) {
      return same(b, ab);
    }

    final Goal append_iterate(Cons<?, ?> a, Object b, Cons<?, ?> ab) {
      return new DelayedGoal(
          conj(
              same(a.car(), ab.car()),
              append(a.cdr(), b, ab.cdr())));
    }

    /**
     * Identifies one sequence as a subset of another sequence, and that the subset has the items in
     * the same order as the other sequence.
     *
     * @param sub the ordered subsequence
     * @param full the full sequence
     */
    public abstract Goal order(Object sub, Object full);

    final Goal order_endOfLists(Void sub, Void full) {
      return UNIT;
    }

    final Goal order_select(Cons<?, ?> sub, Cons<?, ?> full) {
      return conj(
          same(sub.car(), full.car()),
          order(sub.cdr(), full.cdr()));
    }

    final Goal order_skip(Object sub, Cons<?, ?> full) {
      return order(sub, full.cdr());
    }

    /**
     * Identifies two sequences as the reverse of each other.
     */
    public final Goal reverse(Object a, Object b) {
      return reverse(a, b, null);
    }

    /**
     * Identifies two sequences as the reverse of each other, and includes an accumulator argument.
     */
    public abstract Goal reverse(Object a, Object b, Object bTail);

    final Goal reverse_baseCase(Void a, Object b, Object bTail) {
      return same(b, bTail);
    }

    final Goal reverse_iterate(Cons<?, ?> a, Object b, Object bTail) {
      return new DelayedGoal(reverse(a.cdr(), b, Cons.of(a.car(), bTail)));
    }
  }
}
