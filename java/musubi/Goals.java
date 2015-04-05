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

import musubi.EmptyStream;
import musubi.SolveStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic {@link Goal} factories.
 */
public class Goals {
  public static Goal same(final Object u, final Object v) {
    return new Goal() {
      public Stream run(Subst state) {
        state = state.unify(u, v);
        if (state == null) {
          return EmptyStream.INSTANCE;
        }
        return new SolveStep(state, EmptyStream.INSTANCE);
      }
    };
  }

  private static List<Goal> goalList(Goal g1, Goal g2, Goal... gs) {
    List<Goal> allGoals = new ArrayList<>();
    allGoals.add(g1);
    allGoals.add(g2);
    for (Goal g : gs) {
      allGoals.add(g);
    }
    return allGoals;
  }

  public static Goal disj(Goal g1, Goal g2, Goal... gs) {
    final List<Goal> allGoals = goalList(g1, g2, gs);
    return new Goal() {
      public Stream run(Subst s) {
        Stream result = allGoals.get(0).run(s);
        for (int i = 1; i < allGoals.size(); i++) {
          result = result.mplus(allGoals.get(i).run(s));
        }
        return result;
      }
    };
  }

  public static Goal conj(Goal g1, Goal g2, Goal... gs) {
    final List<Goal> allGoals = goalList(g1, g2, gs);

    return new Goal() {
      public Stream run(Subst s) {
        Stream result = allGoals.get(0).run(s);
        for (int i = 1; i < allGoals.size(); i++) {
          result = result.bind(allGoals.get(i));
        }
        return result;
      }
    };
  }

  public static Goal ordero(final Object sub, final Object full) {
    return new Goal() {
      @Override
      public Stream run(Subst s) {
        Var head = new Var();
        Var subTail = new Var();
        Var fullTail = new Var();

        return disj(
            conj(
                same(sub, null),
                same(full, null)),
            conj(
                same(new Cons(head, fullTail), full),
                disj(
                    conj(
                        same(new Cons(head, subTail), sub),
                        ordero(subTail, fullTail)),
                    ordero(sub, fullTail))))
            .run(s);
      }
    };
  }

  private Goals() {}
}
