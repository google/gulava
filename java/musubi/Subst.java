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

import org.pcollections.Empty;
import org.pcollections.PMap;

public final class Subst {
  public static final Subst EMPTY = new Subst(Empty.map());

  private final PMap<Var, Object> map;

  private Subst(PMap<Var, Object> map) {
    this.map = map;
  }

  public PMap<Var, Object> map() {
    return map;
  }

  public Subst ext(Var x, Object v) {
    return new Subst(map.plus(x, v));
  }

  public Object walk(Object u) {
    while ((u instanceof Var) && map.containsKey(u)) {
      u = map.get(u);
    }
    return u;
  }

  public Subst unify(Object u, Object v) {
    u = walk(u);
    v = walk(v);
    if (u == v) {
      return this;
    }
    if (u instanceof Var) {
      return ext((Var) u, v);
    } else if (v instanceof Var) {
      return ext((Var) v, u);
    }
    if ((u == null) || (v == null)) {
      return null;
    }
    if ((u instanceof LogicValue) && (u.getClass() == v.getClass())) {
      return ((LogicValue) u).unify(this, (LogicValue) v);
    }
    if (u.equals(v)) {
      return this;
    }
    return null;
  }

  @Override
  public String toString() {
    return map.toString();
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof Subst)
        && ((Subst) other).map.equals(map);
  }
}
