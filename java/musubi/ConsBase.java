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
package musubi;

import musubi.annotation.MakeLogicValue;

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
@MakeLogicValue(name = "Cons")
public abstract class ConsBase {
  /**
   * Package-protected constructor because only {@link ConsImpl} should subclass.
   */
  ConsBase() {}

  public abstract Object car();
  public abstract Object cdr();

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
      result = new Cons(valueIter.previous(), result);
    }
    return result;
  }
}
