package musubi.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * An annotation processor that reads {@code MakeLogicValue} annotations and creates corresponding
 * implementations of the {@code LogicValue} interface.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_LOGIC_VALUE)
public final class MakeLogicValueProcessor extends AbstractProcessor {
  private static final String capitalizeFirst(String s) {
    if (s.length() == 0) {
      return "";
    }

    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
        PackageElement pkg = (PackageElement) element;

        for (MakeLogicValueMetadata metadata : MakeLogicValueMetadata.forPackage(pkg)) {
          try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(
                pkg.getQualifiedName() + "." + metadata.getName());

            try (Writer writer = file.openWriter()) {
              writer.write("package " + pkg.getQualifiedName() + ";\n");
              writer.write("\n");
              writer.write("public final class " + metadata.getName()
                  + " implements " + ClassNames.LOGIC_VALUE + " {\n");

              // Builder
              writer.write("  public static final class Builder {\n");
              for (String field : metadata.getFields()) {
                writer.write("    private Object " + field + ";\n");
                writer.write("    public Builder set" + capitalizeFirst(field)
                    + "(Object " + field + ") {\n");
                writer.write("      this." + field + " = " + field + ";\n");
                writer.write("      return this;\n");
                writer.write("    }\n");
                writer.write("\n");
              }

              writer.write("    public " + metadata.getName() + " build() {\n");
              writer.write("      return new " + metadata.getName() + "(");
              String delimiter = "";
              for (String field : metadata.getFields()) {
                writer.write(delimiter);
                writer.write(field);
                delimiter = ", ";
              }
              writer.write(");\n");
              writer.write("    }\n");
              writer.write("  }\n");
              writer.write("\n");

              // Constructor
              writer.write("  public " + metadata.getName() + "(");
              delimiter = "";
              for (String field : metadata.getFields()) {
                writer.write(delimiter);
                writer.write("Object " + field);
                delimiter = ", ";
              }
              writer.write(") {\n");
              for (String field : metadata.getFields()) {
                writer.write("    this." + field + " = " + field + ";\n");
              }
              writer.write("  }\n");
              writer.write("\n");

              // Fields and accessors
              for (String field : metadata.getFields()) {
                writer.write("  private final Object " + field + ";\n");
                writer.write("  public Object " + field + "() {\n");
                writer.write("    return " + field + ";\n");
                writer.write("  }\n");
                writer.write("\n");
              }

              // LogicValue method: asMap
              writer.write("  @Override public java.util.Map<String, ?> asMap() {\n");
              writer.write("    java.util.Map<String, Object> map = new java.util.HashMap<>();\n");
              for (String field : metadata.getFields()) {
                writer.write("    map.put(\"" + field + "\", " + field + ");\n");
              }
              writer.write("    return map;\n");
              writer.write("  }\n");
              writer.write("\n");

              // LogicValue method: unify
              writer.write("  @Override public " + ClassNames.SUBST + " unify("
                  + ClassNames.SUBST + " subst, " + ClassNames.LOGIC_VALUE + " other) {\n");
              boolean first = true;
              for (String field : metadata.getFields()) {
                if (!first) {
                  writer.write("    if (subst == null) {\n");
                  writer.write("      return null;\n");
                  writer.write("    }\n");
                }
                writer.write("    subst = subst.unify(this." + field + ", "
                    + "((" + metadata.getName() + ") other)." + field + ");\n");
                first = false;
              }
              writer.write("    return subst;\n");
              writer.write("  }\n");
              writer.write("\n");

              // LogicValue method: replace
              writer.write("  @Override public " + ClassNames.LOGIC_VALUE + " replace("
                  + ClassNames.REPLACER + " replacer) {\n");
              writer.write("    return new " + metadata.getName() + "(");
              delimiter = "";
              for (String field : metadata.getFields()) {
                writer.write(delimiter);
                writer.write("replacer.replace(" + field + ")");
                delimiter = ", ";
              }
              writer.write(");\n");
              writer.write("  }\n");
              writer.write("}\n");
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    return true;
  }
}
