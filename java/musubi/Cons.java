/*
 *  Copyright (c) 2015 Dmitry Neverov and Google
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:

 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.

 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package musubi;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public final class Cons implements LogicValue {
  private final Object a;
  private final Object d;
  public Cons(Object a, Object d) {
    this.a = a;
    this.d = d;
  }
  public Object car() {return a;}
  public Object cdr() {return d;}

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[")
        .append(a);
    Object rest = d;
    while (rest instanceof Cons) {
      Cons restCons = (Cons) rest;
      result.append(",")
          .append(restCons.a);
      rest = restCons.d;
    }
    if (rest != null) {
      result.append("|")
          .append(rest);
    }
    return result
        .append("]")
        .toString();
  }

  @Override
  public Map<String, ?> asMap() {
    Map<String, Object> result = new HashMap<>();
    result.put("car", car());
    result.put("cdr", cdr());
    return result;
  }

  @Override
  public Subst unify(Subst subst, LogicValue other) {
    subst = subst.unify(car(), ((Cons) other).car());
    if (subst == null) {
      return null;
    }
    return subst.unify(cdr(), ((Cons) other).cdr());
  }

  @Override
  public LogicValue replace(Replacer replacer) {
    return new Cons(replacer.replace(car()), replacer.replace(cdr()));
  }

  static Cons list(List<?> values) {
    Cons result = null;
    for (ListIterator<?> valueIter = values.listIterator(values.size()); valueIter.hasPrevious();) {
      result = new Cons(valueIter.previous(), result);
    }
    return result;
  }
}
