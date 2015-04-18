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
import musubi.Var;
import musubi.annotation.MakeGoalFactory;
import musubi.annotation.MakeLogicValue;

/**
 * Represents a queue. This contains its size as a {@code Count} value and its contents as a
 * difference list. This is based on the implementation in chapter 2 of The Craft of Prolog and
 * attributed to Mark Johnson.
 */
@MakeLogicValue
abstract class Queue<S, C> {
  public abstract S size();
  public abstract C contents();

  public static Queue<Void, DiffList<Var, Var>> empty() {
    return of(/*size=*/null, DiffList.empty());
  }

  public static <S, C> Queue<S, C> of(S size, C contents) {
    return new MakeLogicValue_Queue<>(size, contents);
  }

  /**
   * Defines a goal that identifies the final element in a queue.
   */
  @MakeGoalFactory(name = "QueueLast")
  static class LastClauses {
    static Goal goal(Object element, Queue<?, ?> without, Queue<Count<?>, ?> with) {
      return conj(
          same(without.size(), with.size().oneLess()),
          DiffList.O.last(element, without.contents(), with.contents()));
    }
  }

  /**
   * Defines a goal that identifies the first element in a queue.
   */
  @MakeGoalFactory(name = "QueueFirst")
  static class FirstClauses {
    static Goal goal(Object element, Queue<?, ?> without, Queue<Count<?>, ?> with) {
      return conj(
          same(without.size(), with.size().oneLess()),
          DiffList.O.first(element, without.contents(), with.contents()));
    }
  }
}
