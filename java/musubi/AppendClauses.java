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
 * Defines a goal (see {@link Append}) where the first two arguments concatenate to form the third
 * argument. All arguments are {@link Cons} sequences.
 */
@MakeGoalFactory(name = "Append")
public class AppendClauses {
  static Goal finish(Object a, Object b, Object ab) {
    return conj(same(a, null), same(b, ab));
  }

  static Goal iterate(Object a, Object b, Object ab) {
    Var afirst = new Var();
    Var arest = new Var();
    Var abrest = new Var();

    return conj(
        same(new Cons(afirst, arest), a),
        same(new Cons(afirst, abrest), ab),
        Append.o(arest, b, abrest));
  }
}
