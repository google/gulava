package musubi;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a value in a logic program or relational problem. A value belongs to a particular
 * class, which is equivalent to Java classes, and can only unify successfully with other instances
 * of the same class. A value has zero or more fields. Each field may be a logic value, a
 * {@link Var}, or a Java value type such as {@link Integer}, {@link String}, or {@code null}.
 */
public interface LogicValue {
  /**
   * Returns a map view of this value, which includes an entry for every field, even null ones. This
   * may return an unmodifiable map, and may return the same instance if called multiple times. The
   * ordering of the returned map need not be consistent with any other instances, so it is OK to
   * use an implementation such as {@link HashMap} which has an unstable ordering.
   */
  Map<String, ?> asMap();

  /**
   * Extends {@code subst} in such a way that {@code this} equals {@code other}. Returns the
   * resulted substitution. Returns {@code null} if there is no way to unify {@code this} and
   * {@code other}.
   *
   * <p>Implementations may assume that {@code other.getClass() == this.getClass()} and that
   * {@code other} is non-null.
   */
  Subst unify(Subst subst, LogicValue other);

  /**
   * Replaces each field in this value using the given {@code replacer}. Should return an object of
   * the same class.
   */
  LogicValue replace(Replacer replacer);
}
