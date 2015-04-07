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
import static musubi.Goals.disj;
import static musubi.Goals.same;

import musubi.annotation.MakeGoalFactory;

/**
 * Defines a goal (see {@link Order}) which indicates the first argument ({@link Cons} sequence) is
 * a subset of the second argument (also {@link Cons} sequence) and the items in the former are in
 * the same order as the items in the latter.
 */
@MakeGoalFactory(name = "Order")
public class OrderClauses {
  static Goal endOfLists(Object sub, Object full) {
    return conj(
        same(sub, null),
        same(full, null));
  }

  static Goal iterate(Object sub, Object full) {
    Var head = new Var();
    Var subTail = new Var();
    Var fullTail = new Var();

    return conj(
        same(new Cons<>(head, fullTail), full),
        disj(
            conj(
                same(new Cons<>(head, subTail), sub),
                Order.o(subTail, fullTail)),
            Order.o(sub, fullTail)));
  }
}
