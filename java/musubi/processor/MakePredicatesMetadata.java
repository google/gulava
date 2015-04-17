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

import musubi.annotation.MakePredicates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Metadata necessary to generate a predicates subclass. This contains information obtained from the
 * class annotated with @{@link MakePredicates}.
 */
public final class MakePredicatesMetadata {
  private final String name;
  private final List<ExecutableElement> predicateMethods;
  private final TypeElement annotatedType;

  private MakePredicatesMetadata(
      String name, List<ExecutableElement> predicateMethods, TypeElement annotatedType) {
    this.name = name;
    this.predicateMethods = Collections.unmodifiableList(new ArrayList<>(predicateMethods));
    this.annotatedType = annotatedType;
  }

  /** The name of the generated implementation class. */
  public String getName() {
    return name;
  }

  public List<ExecutableElement> getPredicateMethods() {
    return predicateMethods;
  }

  public TypeElement getAnnotatedType() {
    return annotatedType;
  }

  /**
   * Prints errors for any invalid predicate methods found, and returns a list with only the valid
   * ones.
   */
  private static List<ExecutableElement> validatePredicateMethods(
      List<ExecutableElement> predicateMethods, Messager messager) {
    List<ExecutableElement> validated = new ArrayList<>();
    for (ExecutableElement predicateMethod : predicateMethods) {
      int errors = 0;

      if (predicateMethod.getParameters().isEmpty()) {
        messager.printMessage(Diagnostic.Kind.ERROR,
            "Predicate methods must have at least one argument.",
            predicateMethod);
        errors++;
      }

      for (VariableElement parameter : predicateMethod.getParameters()) {
        if (!parameter.asType().toString().equals("java.lang.Object")) {
          messager.printMessage(Diagnostic.Kind.ERROR,
              "All parameters to predicate methods must be of type Object.",
              parameter);
          errors++;
        }
      }

      if (errors == 0) {
        validated.add(predicateMethod);
      }
    }
    return validated;
  }

  /**
   * Returns the metadata stored in a type annotated with @{@link MakePredicates}.
   */
  public static MakePredicatesMetadata of(TypeElement annotatedType, Messager messager) {
    String name = "MakePredicates_" + Processors.generatedClassName(annotatedType);
    List<ExecutableElement> predicateMethods = new ArrayList<>();

    List<? extends ExecutableElement> allMethods =
        ElementFilter.methodsIn(annotatedType.getEnclosedElements());
    for (ExecutableElement method : allMethods) {
      if (!method.getModifiers().contains(Modifier.PRIVATE)
          && !method.getModifiers().contains(Modifier.STATIC)
          && method.getModifiers().contains(Modifier.ABSTRACT)) {
        predicateMethods.add(method);
      }
    }

    predicateMethods = validatePredicateMethods(predicateMethods, messager);

    return new MakePredicatesMetadata(name, predicateMethods, annotatedType);
  }
}
