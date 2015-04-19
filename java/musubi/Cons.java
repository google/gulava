package musubi;

import java.util.List;
import java.util.ListIterator;

class Cons {
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

  static Cons list(List<?> values) {
    Cons result = null;
    for (ListIterator<?> valueIter = values.listIterator(values.size()); valueIter.hasPrevious();) {
      result = new Cons(valueIter.previous(), result);
    }
    return result;
  }
}


