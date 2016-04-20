/*
 *  Copyright (c) 2016 The Gulava Authors
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

/**
 * A goal that is a conjoining of several subgoals. This goal generates substitutions that
 * satisfy all subgoals.
 */
public final class ConjGoal extends CompositeGoal {
  ConjGoal(Goal g1, Goal g2, Goal[] gs) {
    super("conj", g1, g2, gs);
  }

  ConjGoal(Goal[] gs) {
    super("conj", gs);
  }

  @Override
  public Stream run(Subst s) {
    Stream result = allGoals[0].run(s);
    for (int i = 1; i < allGoals.length; i++) {
      result = result.bind(allGoals[i]);
    }
    return result;
  }

  /**
   * Returns a new instance which interleaves the subgoals in this instance with other goals.
   */
  public ConjGoal interleave(Goal g1, Goal... gs) {
    Goal[] newAllGoals = new Goal[gs.length + allGoals.length + 1];
    int newI = 0;
    int thisI = 0;
    newAllGoals[newI++] = allGoals[thisI++];
    newAllGoals[newI++] = g1;
    int thatI = 0;
    while (newI < newAllGoals.length) {
      if (thisI < allGoals.length) {
        newAllGoals[newI++] = allGoals[thisI++];
      }
      if (thatI < gs.length) {
        newAllGoals[newI++] = gs[thatI++];
      }
    }
    return new ConjGoal(newAllGoals);
  }
}
