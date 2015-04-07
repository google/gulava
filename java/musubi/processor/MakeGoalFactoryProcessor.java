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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * An annotation processor that reads classes annoated with @{@code MakeGoalFactory} and creates
 * a corresponding class for each one that constructs a certain goal.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_GOAL_FACTORY)
public final class MakeGoalFactoryProcessor extends AbstractProcessor {
  /**
   * Returns each argument name as it would appear in a signature as the type {@link Object}.
   * Includes comma delimiters if there is more than one argument. Does not include enclosing
   * parenthesis.
   */
  private static String paramList(Iterable<String> argNames) {
    List<String> parameters = new ArrayList<>();
    for (String argName : argNames) {
      // Prepend "Object" in generated argument list with "final"

      // This makes the generated source code valid when compiling with JDK 7. Otherwise,
      // we are trying to access a non-final variable from within an anonymous inner
      // class, which is not supported until JDK 8.
      parameters.add("final Object " + argName);
    }
    return Processors.join(", ", parameters);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (AnnotatedType annotatedType : AnnotatedType.all(annotations, roundEnv)) {
      MakeGoalFactoryMetadata metadata =
          MakeGoalFactoryMetadata.of(annotatedType.getType(), processingEnv.getMessager());
      String paramList = paramList(metadata.getArgNames());
      String argList = Processors.join(", ", metadata.getArgNames());

      try  (Writer writer = annotatedType.openWriter(processingEnv, metadata.getName())) {
        writer.write("public class " + metadata.getName() + " {\n");

        // Goal factory method: i (inline)
        writer.write("  public static " + ClassNames.GOAL + " i(" + paramList + ") {\n");
        List<String> goalMethodInvocations = new ArrayList<>();
        for (ExecutableElement goalMethod : metadata.getGoalMethods()) {
          String invocation = String.format("%s.%s(%s)",
              metadata.getAnnotatedType().getQualifiedName(),
              goalMethod.getSimpleName(),
              argList);

          goalMethodInvocations.add(invocation);
        }
        writer.write("    return ");
        if (goalMethodInvocations.size() == 1) {
          writer.write(goalMethodInvocations.get(0));
        } else {
          writer.write(
              ClassNames.GOALS + ".disj(" + Processors.join(", ", goalMethodInvocations) + ")");
        }
        writer.write(";\n");
        writer.write("  }\n");

        // Goal factory method: o (normal)
        writer.write("  public static " + ClassNames.GOAL + " o(" + paramList + ") {\n");
        writer.write("    return new " + ClassNames.GOAL + "() {\n");
        writer.write("      @java.lang.Override\n");
        writer.write("      public " + ClassNames.STREAM + " run("
            + ClassNames.SUBST + " __stream__) {\n");
        writer.write("        return i(" + argList + ").run(__stream__);\n");
        writer.write("      }\n");
        writer.write("    };\n");
        writer.write("  }\n");

        // Goal factory method: d (delayed)
        writer.write("  public static " + ClassNames.GOAL + " d(" + paramList + ") {\n");
        writer.write("    return new " + ClassNames.DELAYED_GOAL + "(o(" + argList + "));\n");
        writer.write("  }\n");

        writer.write("}\n");
      } catch (IOException e) {
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.ERROR, e.toString(), annotatedType.getType());
        e.printStackTrace();
      }
    }
    return true;
  }
}
