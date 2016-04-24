/*
 *  Copyright (c) 2015 The Gulava Authors
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
 * Basic {@link Goal} factories.
 */
public class Goals {
  /**
   * A goal that simply succeeds once without affecting substitutions.
   */
  public static final Goal UNIT = new Goal() {
    @Override
    public Stream run(Subst s) {
      return s;
    }

    @Override
    public String toString() {
      return "UNIT";
    }
  };

  /**
   * A goal that fails, always returning an empty stream.
   */
  public static final Goal FAIL = new Goal() {
    @Override
    public Stream run(Subst s) {
      return EmptyStream.INSTANCE;
    }

    @Override
    public String toString() {
      return "FAIL";
    }
  };

  public static Goal same(final Object u, final Object v) {
    return new Goal() {
      @Override
      public Stream run(Subst state) {
        state = state.unify(u, v);
        if (state == null) {
          return EmptyStream.INSTANCE;
        }
        return state;
      }

      @Override
      public String toString() {
        return String.format("{%s == %s}", u, v);
      }
    };
  }

  /**
   * Returns a goal that generates substitutions which satisfy any one subgoal.
   */
  public static DisjGoal disj(Goal g1, Goal g2, Goal... gs) {
    return new DisjGoal(g1, g2, gs);
  }

  /**
   * Returns a goal that generates substitutions that satisfy two or more subgoals.
   */
  public static ConjGoal conj(Goal g1, Goal g2, Goal... gs) {
    return new ConjGoal(g1, g2, gs);
  }

  private Goals() {}
}
