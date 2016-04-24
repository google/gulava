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

import java.io.IOException;

/**
 * A goal which will not be run more than once after it has already passed. This is not just an
 * optimization - it can also change the result stream. If a {@code CachedGoal} wraps a
 * {@link DisjGoal} and the {@code CachedGoal} appears multiple times in some larger composite goal,
 * then the {@link DisjGoal} is guaranteed to only {@link Goal#run(Subst)} once, and so will choose
 * the same alternate goal each time it appears. See the test
 * {@link CachedGoalTest#disjIsNotReevaluated()} for an example of this.
 */
public final class CachedGoal implements Dumpable, Goal {
  private final CachedGoal prerequisite;
  private final Goal delegate;

  public CachedGoal(Goal delegate) {
    this(null, delegate);
  }

  public CachedGoal(CachedGoal prerequisite, Goal delegate) {
    this.prerequisite = prerequisite;
    this.delegate = delegate;
  }

  @Override
  public Stream run(Subst s) {
    if (prerequisite != null && s.get(prerequisite) == null) {
      return EmptyStream.INSTANCE;
    }
    Object cached = s.get(this);
    if (cached == null) {
      return delegate.run(s.ext(this, true));
    } else {
      return s;
    }
  }

  @Override
  public void dump(Dumper dumper) throws IOException {
    dumper.dump(
        "CachedGoal@" + Integer.toString(System.identityHashCode(this), 36),
        delegate);
  }
}
