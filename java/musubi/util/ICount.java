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

import musubi.Var;
import musubi.annotation.MakeLogicValue;

/**
 * Represents a numeric count. This structure is useful if you are not performing any non-trivial
 * calculations except adding or subtracting small integers.
 *
 * <p>This has a single field - the counter is equal to 1 + the "count value" of the field. The
 * count value of the field is either 0 (for null) or some natural number (for instances of this
 * class).
 */
@MakeLogicValue(name = "Count")
abstract class ICount<C> {
  /**
   * The value whose count is one less than this one.
   */
  public abstract C oneLess();

  /**
   * Returns the string representation. This counts the number of nested instances of this
   * class so that the human reading it doesn't have to, and includes that integer in the result. If
   * the first nested element found is not an instance of this class, then it includes
   * {@code + nested.toString()} in the result. Generally, {@code nested} in this case is a
   * {@link Var}.
   */
  @Override
  public final String toString() {
    int countValue = 1;
    Object nested = oneLess();
    while (nested instanceof ICount) {
      countValue++;
      nested = ((ICount) nested).oneLess();
    }
    String maybePlusSymbol = "";
    Object maybeInnerVar = "";
    if (nested != null) {
      maybePlusSymbol = "+";
      maybeInnerVar = nested;
    }
    return String.format("Count{%d%s%s}", countValue, maybePlusSymbol, maybeInnerVar);
  }
}
