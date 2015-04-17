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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * An annotation processor that reads classes annoated with @{@code MakeGoalFactory} and creates
 * a corresponding class for each one that constructs a certain goal.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_GOAL_FACTORY)
public final class MakeGoalFactoryProcessor extends AbstractProcessor {
  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Information about the invocation of a certain clause required by the goal factory method. A
   * goal factory method needs at least one clause to be valid - each clause represents an
   * alternative to reaching the goal. This means each clause invocation should be disj'd together
   * to create the final {@code Goal}.
   */
  private static final class ClauseInvocation {
    /**
     * The goal expressions which are needed to conj together to fulfill this clause. This
     * includes the clause invocation itself as well as "same()" (unification) goals which
     * unify the Object reference given to the goal factory method with the kind of reference
     * required by the clause method.
     */
    final List<String> subGoals;

    /** Lines, each holding a statement, that should appear before the invocation. */
    final List<String> preparationStatements;

    ClauseInvocation(List<String> subGoals, List<String> preparationStatements) {
      this.subGoals = subGoals;
      this.preparationStatements = preparationStatements;
    }
  }

  private ClauseInvocation clauseInvocation(
      MakeGoalFactoryMetadata metadata, ExecutableElement clauseMethod, Gensymer boundIds) {
    // The actual arguments to pass to the clause method. If the clause method accepts
    // "Object" for an argument, this can just be the same value passed to the predicate
    // method.
    List<String> decomposedArgList = new ArrayList<>();

    List<String> subGoals = new ArrayList<>();
    List<String> preparationStatements = new ArrayList<>();

    for (VariableElement parameter : clauseMethod.getParameters()) {
      TypeMirror parameterType = parameter.asType();
      String parameterName = parameter.getSimpleName().toString();

      if ((parameterType instanceof DeclaredType)
          && Processors.qualifiedName((DeclaredType) parameterType)
              .contentEquals("java.lang.Object")) {
        // No need to decompose because the clause method accepts an Object reference.
        decomposedArgList.add(parameterName);
      } else {
        // We need to decompose this argument. Unify the Object reference passed to the goal
        // factory method to a reference of the desired type.
        String boundId = boundIds.gensym();
        decomposedArgList.add(boundId);
        String fresh = new FreshInstantiation(parameter, processingEnv.getMessager())
            .visit(parameterType);

        preparationStatements.add(String.format("%s %s = %s;", parameterType, boundId, fresh));
        subGoals.add(String.format("%s.same(%s, %s)", ClassNames.GOALS, boundId, parameterName));
      }
    }

    // subGoals already has all the unification goals needed, if any. Now just add the
    // delegation to the clause method.
    subGoals.add(
        String.format("%s.%s(%s)",
            metadata.getAnnotatedType().getQualifiedName(),
            clauseMethod.getSimpleName(),
            Processors.join(", ", decomposedArgList)));

    return new ClauseInvocation(subGoals, preparationStatements);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (AnnotatedType annotatedType : AnnotatedType.all(annotations, roundEnv)) {
      MakeGoalFactoryMetadata metadata =
          MakeGoalFactoryMetadata.of(annotatedType.getType(), processingEnv.getMessager());
      String paramList = Processors.objectParamList(metadata.getArgNames());
      String argList = Processors.join(", ", metadata.getArgNames());

      try  (Writer writer = annotatedType.openWriter(processingEnv, metadata.getName())) {
        writer.write("public class " + metadata.getName() + " {\n");

        // Goal factory method: i (inline)
        writer.write("  public static " + ClassNames.GOAL + " i(" + paramList + ") {\n");
        Gensymer boundIds = new Gensymer("__bound%s__");

        List<String> clauseInvocationExpressions = new ArrayList<>();

        for (ExecutableElement clauseMethod : metadata.getClauseMethods()) {
          ClauseInvocation invocation = clauseInvocation(metadata, clauseMethod, boundIds);
          for (String preparationStatement : invocation.preparationStatements) {
            writer.write("    " + preparationStatement + "\n");
          }
          clauseInvocationExpressions.add(Processors.compoundGoal("conj", invocation.subGoals));
        }
        writer.write("    return "
            + Processors.compoundGoal("disj", clauseInvocationExpressions) + ";\n");
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
