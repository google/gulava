package musubi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.PACKAGE)
public @interface MakeLogicValue {
  String name();
  String[] fields();
}
