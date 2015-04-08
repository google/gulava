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

import musubi.annotation.MakeGoalFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Contains information derived from a {@code @MakeGoalFactory} annotation and the type that it
 * annotates.
 */
public final class MakeGoalFactoryMetadata {
  private final String name;
  private final List<String> argNames;
  private final List<ExecutableElement> goalMethods;
  private final TypeElement annotatedType;

  MakeGoalFactoryMetadata(String name, List<String> argNames, List<ExecutableElement> goalMethods,
      TypeElement annotatedType) {
    this.name = name;
    this.argNames = argNames;
    this.goalMethods = Collections.unmodifiableList(new ArrayList<>(goalMethods));
    this.annotatedType = annotatedType;
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
  public List<String> getArgNames() {
    return argNames;
  }

  /**
   * The original methods which construct goals. This is not every method in the annotated type.
   * Non-static methods and private methods are filtered out.
   */
  public List<ExecutableElement> getGoalMethods() {
    return goalMethods;
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
    List<ExecutableElement> goalMethods = new ArrayList<>();

    String name = annotation.name();
    if (name == null || name.isEmpty()) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Require non-empty name in @MakeGoalFactory annotation.",
          annotatedType);
      name = "";
    }

    List<String> argNames = null;
    List<? extends ExecutableElement> allMethods =
        ElementFilter.methodsIn(annotatedType.getEnclosedElements());
    METHODS: for (ExecutableElement method : allMethods) {
      if (method.getModifiers().contains(Modifier.PRIVATE)
          || !method.getModifiers().contains(Modifier.STATIC)) {
        continue;
      }

      if (argNames == null) {
        argNames = new ArrayList<>();
        for (VariableElement parameters : method.getParameters()){
          argNames.add(parameters.getSimpleName().toString());
        }
        goalMethods.add(method);
        continue;
      }

      if (method.getParameters().size() != argNames.size()) {
        // This method appears to be a clause but does not have the same number of parameters as the
        // first clause. That means it cannot be used in conjunction to form a single goal, so we
        // exclude it.
        messager.printMessage(Diagnostic.Kind.ERROR,
            "Expected this method to have " + argNames.size() + " parameters to match "
            + allMethods.get(0).getSimpleName() + " but there are only "
            + method.getParameters().size() + " on this method.",
            method);
        continue;
      }

      // Make sure the argument names match the names of the first clause method. This not only
      // makes generating code easier (since we can use the VariableElement for the parameters of
      // the methods more interchangeably), but it also keeps the manually-written code consistent
      // and sane-looking.
      for (int i = 0; i < argNames.size(); i++) {
        VariableElement parameter = method.getParameters().get(i);
        if (!parameter.getSimpleName().contentEquals(argNames.get(i))) {
          messager.printMessage(Diagnostic.Kind.ERROR,
              "Expected this argument to have the name " + argNames.get(i) + " to match "
              + allMethods.get(0),
              parameter);
          continue METHODS;
        }
      }

      goalMethods.add(method);
    }

    if (goalMethods.isEmpty()) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Expect at least one static, non-private method in class annotated with @MakeGoalFactory",
          annotatedType);
    }

    return new MakeGoalFactoryMetadata(name, argNames, goalMethods, annotatedType);
  }
}
