package musubi;

import org.pcollections.Empty;
import org.pcollections.PMap;

final class Subst {
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
    while (MusubiDemo.isVar(u) && map.containsKey(u)) {
      u = map.get(u);
    }
    return u;
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
