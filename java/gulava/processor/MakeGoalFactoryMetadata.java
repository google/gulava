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
package gulava.processor;

import gulava.annotation.MakeGoalFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Contains information derived from a {@code @MakeGoalFactory} annotation and the type that it
 * annotates.
 */
public final class MakeGoalFactoryMetadata {
  private final String name;
  private final List<Param> params;
  private final List<ExecutableElement> clauseMethods;
  private final TypeElement annotatedType;

  MakeGoalFactoryMetadata(String name, List<Param> params, List<ExecutableElement> clauseMethods,
      TypeElement annotatedType) {
    this.name = name;
    this.params = Collections.unmodifiableList(new ArrayList<>(params));
    this.clauseMethods = Collections.unmodifiableList(new ArrayList<>(clauseMethods));
    this.annotatedType = annotatedType;
  }

  private static final class Param {
    final String type;
    final String name;

    Param(String type, String name) {
      this.type = type;
      this.name = name;
    }
  }

  /**
   * The name of the factory class to generate.
   */
  public String getName() {
    return name;
  }

  /**
   * Names of the arguments of the goal. Each goal method should have the same number and names of
   * each argument.
   */
  public String getParamNames() {
    List<String> names = new ArrayList<>();
    for (Param param : params) {
      names.add(param.name);
    }
    return Processors.join(",", names);
  }

  public String getParamList() {
    List<String> parameters = new ArrayList<>();
    for (Param param : params) {
      parameters.add(String.format("final %s %s", param.type, param.name));
    }
    return Processors.join(",", parameters);
  }

  /**
   * The original methods which construct goals. This is not every method in the annotated type.
   * Non-static methods and private methods are filtered out.
   */
  public List<ExecutableElement> getClauseMethods() {
    return clauseMethods;
  }

  public TypeElement getAnnotatedType() {
    return annotatedType;
  }

  /**
   * Returns the metadata stored in a single annotation of type {@code MakeGoalFactory} and the type
   * it annotates.
   */
  public static MakeGoalFactoryMetadata of(TypeElement annotatedType, Messager messager) {
    MakeGoalFactory annotation = annotatedType.getAnnotation(MakeGoalFactory.class);
    List<ExecutableElement> clauseMethods = new ArrayList<>();

    String name = annotation.name();
    if (name == null || name.isEmpty()) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Require non-empty name in @MakeGoalFactory annotation.",
          annotatedType);
      name = "";
    }

    List<Param> params = new ArrayList<>();
    List<? extends ExecutableElement> allMethods =
        ElementFilter.methodsIn(annotatedType.getEnclosedElements());
    for (ExecutableElement method : allMethods) {
      if (method.getModifiers().contains(Modifier.PRIVATE)
          || !method.getModifiers().contains(Modifier.STATIC)) {
        // Ignore this method.
        continue;
      } else if (clauseMethods.isEmpty()) {
        // This is the first clause method. Get parameter information.
        for (VariableElement param : method.getParameters()) {
          TypeMirror rawType = param.asType();
          String type = new IsPassThroughType().visit(rawType)
              ? rawType.toString() : "java.lang.Object";
          params.add(new Param(type, param.getSimpleName().toString()));
        }
        clauseMethods.add(method);
      } else if (method.getParameters().size() != params.size()) {
        // This method appears to be a clause but does not have the same number of parameters as the
        // first clause. That means it cannot be used in conjunction to form a single goal, so we
        // exclude it.
        messager.printMessage(Diagnostic.Kind.ERROR,
            "Expected this method to have " + params.size() + " parameter(s) to match "
            + allMethods.get(0).getSimpleName() + " but it has: "
            + method.getParameters().size(),
            method);

      // Make sure the argument names match the names of the first clause method. This not only
      // makes generating code easier (since we can use the VariableElement for the parameters of
      // the methods more interchangeably), but it also keeps the manually-written code consistent
      // and sane-looking.
      } else if (!Processors.printArgNamesMatchError(messager, allMethods.get(0), method)) {
        clauseMethods.add(method);
      }
    }

    if (clauseMethods.isEmpty()) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Expect at least one static, non-private method in class annotated with @MakeGoalFactory",
          annotatedType);
    }

    return new MakeGoalFactoryMetadata(name, params, clauseMethods, annotatedType);
  }
}
