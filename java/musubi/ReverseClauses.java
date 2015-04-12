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

import static musubi.Goals.conj;
import static musubi.Goals.same;

import musubi.annotation.MakeGoalFactory;

/**
 * Defines a goal (see {@link Reverse}) where the first argument is the same sequence as the second
 * argument, but in reverse order. Arguments are treated as {@link Cons} sequences.
 */
@MakeGoalFactory(name = "Reverse")
public class ReverseClauses {
  @MakeGoalFactory(name = "ReverseWithAccum")
  public static class WithAccum {
    static Goal finish(Void a, Object b, Object bTail) {
      return same(b, bTail);
    }

    static Goal iterate(Cons<?, ?> a, Object b, Object bTail) {
      return ReverseWithAccum.d(a.cdr(), b, Cons.of(a.car(), bTail));
    }
  }

  static Goal delegate(Object a, Object b) {
    return ReverseWithAccum.i(a, b, null);
  }
}
