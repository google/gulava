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
