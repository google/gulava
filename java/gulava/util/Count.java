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
import static gulava.Goals.same;

import gulava.Goal;
import gulava.Var;
import gulava.annotation.MakeLogicValue;
import gulava.annotation.MakePredicates;

/**
 * Represents a numeric count. This structure is useful if you are not performing any non-trivial
 * calculations except adding or subtracting small integers.
 *
 * <p>This has a single field - the counter is equal to 1 + the "count value" of the field. The
 * count value of the field is either 0 (for null) or some natural number (for instances of this
 * class).
 */
@MakeLogicValue
public abstract class Count<C> {
  /**
   * The value whose count is one less than this one.
   */
  public abstract C oneLess();

  public static Count<?> fromInt(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Must be non-negative: " + value);
    }

    Count<?> result = null;
    while (value > 0) {
      result = Count.of(result);
      value--;
    }

    return result;
  }

  public static <C> Count<C> of(C oneLess) {
    return new MakeLogicValue_Count<>(oneLess);
  }

  /**
   * Returns the string representation. This counts the number of nested instances of this
   * class so that the human reading it doesn't have to, and includes that integer in the result. If
   * the first nested element found is not an instance of this class, then it includes
   * {@code + nested.toString()} in the result. Generally, {@code nested} in this case is a
   * {@link Var}.
   */
  @Override
  public final String toString() {
    return toString("Count", this);
  }

  /**
   * Similar to {@link #toString()}, but allows using a label other than {@code "Count"} (this
   * class' name) in the format. This method will give a meaningful result when {@code count} is
   * {@code null}.
   */
  public static String toString(String label, Object count) {
    int countValue = 0;
    while (count instanceof Count) {
      countValue++;
      count = ((Count) count).oneLess();
    }
    String maybePlusSymbol = "";
    Object maybeInnerVar = "";
    if (count != null) {
      maybePlusSymbol = "+";
      maybeInnerVar = count;
    }
    return String.format("%s{%d%s%s}", label, countValue, maybePlusSymbol, maybeInnerVar);
  }

  public static final Goals O = new MakePredicates_Count_Goals();

  /**
   * Goals related to numbers expressed as {@link Count} instances.
   */
  @MakePredicates
  public static abstract class Goals {
    /**
     * Indicates that {@code a} is a lesser count than {@code b}.
     */
    public abstract Goal less(Object a, Object b);

    final Goal less_nonZero(Count<?> a, Count<?> b) {
      return less(a.oneLess(), b.oneLess());
    }

    final Goal less_zero(Void a, Count<?> b) {
      return UNIT;
    }

    /**
     * Indicates that {@code a} is a lesser or equal count to {@code b}.
     */
    public abstract Goal lessOrEqual(Object a, Object b);

    final Goal lessOrEqual_base(Void a, Object b) {
      return UNIT;
    }

    final Goal lessOrEqual_iterate(Count<?> a, Count<?> b) {
      return lessOrEqual(a.oneLess(), b.oneLess());
    }
  }
}
