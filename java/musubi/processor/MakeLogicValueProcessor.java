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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * An annotation processor that reads {@code MakeLogicValue} annotations and creates corresponding
 * implementations of the {@code LogicValue} interface.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_LOGIC_VALUE)
public final class MakeLogicValueProcessor extends AbstractProcessor {
  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (AnnotatedType annotatedType : AnnotatedType.all(annotations, roundEnv)) {
      TypeElement interfaze = annotatedType.getType();
      MakeLogicValueMetadata metadata =
          MakeLogicValueMetadata.forInterface(interfaze, processingEnv.getMessager());
      try  (Writer writer = annotatedType.openWriter(processingEnv, metadata.getName())) {
        String extendsClause = "";
        String implementsClause = " implements " + ClassNames.LOGIC_VALUE;
        switch (interfaze.getKind()) {
          case INTERFACE:
            implementsClause += ", " + interfaze.getQualifiedName();
            break;
          case CLASS:
            extendsClause += " extends " + interfaze.getQualifiedName();
            break;
          default:
            throw new IllegalStateException("unexpected kind: " + interfaze.getKind());
        }

        writer.write("public final class " + metadata.getName()
            + metadata.typeParametersAlligator()
            + extendsClause + implementsClause + " {\n");

        // Builder
        writer.write("  public static final class Builder"
            + metadata.typeParametersAlligator() + " {\n");
        for (LogicValueField field : metadata.getFields()) {
          writer.write("    private " + field.getTypeAndName() + ";\n");
          writer.write("    public Builder" + metadata.typeParametersAlligator()
              + " " + field.getSetterMethodName()
              + "(" + field.getTypeAndName() + ") {\n");
          writer.write("      this." + field + " = " + field + ";\n");
          writer.write("      return this;\n");
          writer.write("    }\n");
          writer.write("\n");
        }

        writer.write("    public " + metadata.getName() + metadata.typeParametersAlligator()
            + " build() {\n");
        writer.write("      return new " + metadata.getName() + metadata.typeParametersAlligator()
            + "(");
        String delimiter = "";
        for (LogicValueField field : metadata.getFields()) {
          writer.write(delimiter);
          writer.write(field.getName());
          delimiter = ", ";
        }
        writer.write(");\n");
        writer.write("    }\n");
        writer.write("  }\n");
        writer.write("\n");

        // Constructor
        writer.write("  public " + metadata.getName() + "(");
        delimiter = "";
        for (LogicValueField field : metadata.getFields()) {
          writer.write(delimiter);
          writer.write(field.getTypeAndName());
          delimiter = ", ";
        }
        writer.write(") {\n");
        for (LogicValueField field : metadata.getFields()) {
          writer.write("    this." + field + " = " + field + ";\n");
        }
        writer.write("  }\n");
        writer.write("\n");

        // Fields and accessors
        for (LogicValueField field : metadata.getFields()) {
          writer.write("  private final " + field.getTypeAndName() + ";\n");
          writer.write("  public " + field.getTypeAndName() + "() {\n");
          writer.write("    return " + field + ";\n");
          writer.write("  }\n");
          writer.write("\n");
        }

        // LogicValue method: asMap
        writer.write("  @Override public java.util.Map<String, ?> asMap() {\n");
        writer.write("    java.util.Map<String, Object> map = new java.util.HashMap<>();\n");
        for (LogicValueField field : metadata.getFields()) {
          writer.write("    map.put(\"" + field + "\", " + field + ");\n");
        }
        writer.write("    return map;\n");
        writer.write("  }\n");
        writer.write("\n");

        // LogicValue method: unify
        writer.write("  @Override public " + ClassNames.SUBST + " unify("
            + ClassNames.SUBST + " subst, " + ClassNames.LOGIC_VALUE + " other) {\n");
        boolean first = true;
        for (LogicValueField field : metadata.getFields()) {
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
        writer.write("    return new " + metadata.getName() + "<>(");
        delimiter = "";
        for (LogicValueField field : metadata.getFields()) {
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
        for (LogicValueField field : metadata.getFields()) {
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
        for (LogicValueField field : metadata.getFields()) {
          writer.write("    code *= 31;\n");
          writer.write("    if (this." + field + " != null) {\n");
          writer.write("      code ^= this." + field + ".hashCode();\n");
          writer.write("    }\n");
        }
        writer.write("    return code;\n");
        writer.write("  }\n");

        // Object method: toString
        if (metadata.autoDefineToString()) {
          writer.write("  @Override public String toString() {\n");
          writer.write("    StringBuilder s = new StringBuilder(\""
              + metadata.getName() + "(\");\n");
          first = true;
          for (LogicValueField field : metadata.getFields()) {
            if (!first) {
              writer.write("    s.append(\", \");\n");
            }
            first = false;

            writer.write("    s.append(this." + field + ");\n");
          }
          writer.write("    return s.append(')').toString();");
          writer.write("  }\n");
        }
        writer.write("}\n");
      } catch (IOException e) {
        Processors.print(processingEnv.getMessager(), e, interfaze);
      }
    }

    return true;
  }
}


