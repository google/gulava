package musubi;

/**
 * An object that replaces objects with other objects. This can be used, for example, to apply
 * variable substitutions to a {@link LogicValue} to prepare it for printing or to substitute bound
 * variables.
 */
public interface Replacer {
  /**
   * Replaces an object. This method can return the same object if it should not be replaced. This
   * method can return {@code null} if the given object really should be replaced with {@code null}.
   */
  Object replace(Object original);
}
