/*
 *  Copyright (c) 2015 The Gulava Authors
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
package gulava.processor;

import gulava.annotation.MakePredicates;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * An annotation processor that reads classes annoated with @{@link MakePredicates} and creates
 * a in implementing class for each annotated type.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_PREDICATES)
public final class MakePredicatesProcessor extends AnnotatedType.Processor {
  @Override
  protected void process(AnnotatedType annotatedType, Messager messager) throws IOException {
    MakePredicatesMetadata metadata =
        MakePredicatesMetadata.of(annotatedType.getType(), messager);
    try (Writer writer = annotatedType.openWriter(metadata.getName())) {
      writer.write("public class " + metadata.getName()
          + " extends " + metadata.getAnnotatedType().getQualifiedName()
          + " {\n");

      // Write constructors that delegate to accessible superclass constructors
      for (ExecutableElement constructor : metadata.getConstructors()) {
        List<? extends VariableElement> parameters = constructor.getParameters();

        writer.write("\n");
        writer.write("  " + metadata.getName() + "(");
        String delimiter = "";
        for (VariableElement parameter : parameters) {
          writer.write(delimiter);
          delimiter = ", ";
          writer.write(parameter.asType().toString() + " " + parameter.getSimpleName());
        }
        writer.write(") {\n");
        writer.write("    super("
            + Processors.join(", ", Processors.argNames(constructor)) + ");\n");
        writer.write("  }\n");
      }

      for (Predicate predicate : metadata.getPredicates()) {
        GoalExpressions expressions = new GoalExpressions("this", messager);
        String inlineName = "__" + predicate.getName() + "Inline__";
        expressions.writeInlineMethod(writer, "private", inlineName, predicate.getClauses(),
            Processors.objectParamList(predicate.argNames()));
        writer.write("\n");
        writer.write("  @java.lang.Override\n");
        writer.write("  public " + ClassNames.GOAL + " " + predicate.getName() + "("
            + Processors.objectParamList(predicate.argNames()) + ") {\n");
        writer.write("    return new " + ClassNames.GOAL + "() {\n");
        writer.write("      @java.lang.Override\n");
        writer.write("      public " + ClassNames.STREAM + " run("
            + ClassNames.SUBST + " __subst__) {\n");
        writer.write("        return " + metadata.getName() + ".this." + inlineName + "("
            + Processors.join(", ", predicate.argNames()) + ").run(__subst__);\n");
        writer.write("      }\n");
        writer.write("\n");
        writer.write("      @java.lang.Override\n");
        writer.write("      public java.lang.String toString() {\n");
        writer.write("        return \"" + predicate.getName() + "(\"");
        String delimiter = "";
        for (String argName : predicate.argNames()) {
          writer.write(" + " + delimiter + argName);
          delimiter = " \", \" + ";
        }
        writer.write(" + \")\";\n");
        writer.write("      }\n");
        writer.write("    };\n");
        writer.write("  }\n");
      }

      writer.write("}\n");
    }
  }
}
