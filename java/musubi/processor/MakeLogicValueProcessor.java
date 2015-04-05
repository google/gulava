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

              // Object method: equals
              writer.write("  @Override public boolean equals(Object o) {\n");
              writer.write("    if (o == null) return false;\n");
              writer.write("    if (o.getClass() != getClass()) return false;\n");
              writer.write("\n");
              writer.write("    " + metadata.getName() + " other = (" + metadata.getName() + ") o;\n");
              for (String field : metadata.getFields()) {
                writer.write("    if (this." + field + " == null) {\n");
                writer.write("      if (other." + field + " != null) return false;\n");
                writer.write("    } else if (!this." + field + ".equals(other." + field + ")) {\n");
                writer.write("      return false;\n");
                writer.write("    }\n");
                writer.write("\n");
              }
              writer.write("    return true;\n");
              writer.write("  }\n");

              // Object method: hashCode
              writer.write("  @Override public int hashCode() {\n");
              writer.write("    int code = 1;\n");
              for (String field : metadata.getFields()) {
                writer.write("    code *= 31;\n");
                writer.write("    if (this." + field + " != null) {\n");
                writer.write("      code ^= this." + field + ".hashCode();\n");
                writer.write("    }\n");
              }
              writer.write("    return code;\n");
              writer.write("  }\n");

              // Object method: toString
              writer.write("  @Override public String toString() {\n");
              writer.write("    StringBuilder s = new StringBuilder(\"" + metadata.getName() + "(\");\n");
              first = true;
              for (String field : metadata.getFields()) {
                if (!first) {
                  writer.write("    s.append(\", \");\n");
                }
                first = false;

                writer.write("    s.append(this." + field + ");\n");
              }
              writer.write("    return s.append(')').toString();");
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
