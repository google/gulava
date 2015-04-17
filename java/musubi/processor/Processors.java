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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

/**
 * Utility code for implementing annotation processors.
 */
public class Processors {
  /**
   * Reports an exception as an error to the given messager. This includes printing the stacktrace
   * to stderr.
   *
   * @param messager the messager to report the error to
   * @param throwable the exception to report
   * @param element the element which the exception is related to
   */
  public static void print(Messager messager, Throwable throwable, Element element) {
    messager.printMessage(Diagnostic.Kind.ERROR, throwable.toString(), element);
    throwable.printStackTrace();
  }

  /**
   * Joins the string representation of each object in {@code objects} into a single string.
   * {@code delimiter} is used to separate the objects, but is not added before the first or after
   * the last object.
   */
  public static String join(String delimiter, Iterable<?> objects) {
    StringBuilder list = new StringBuilder();
    boolean first = true;
    for (Object object : objects) {
      if (!first) {
        list.append(delimiter);
      }
      first = false;
      list.append(object);
    }
    return list.toString();
  }

  /**
   * Returns a string equivalent to {@code s} but with the first character capitalized.
   */
  public static final String capitalizeFirst(String s) {
    if (s.length() == 0) {
      return "";
    }

    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  /**
   * Returns the fully qualified name of the {@link TypeElement} indicated by the mirror
   * {@code type}.
   */
  public static Name qualifiedName(DeclaredType type) {
    return ((TypeElement) type.asElement()).getQualifiedName();
  }

  /**
   * Checks if the argument names match between two methods. If they do not match, prints
   * an error to {@code messager} and returns {@code true}. Otherwise, returns {@code false}.
   * This method assumes that the number of arguments is the same.
   */
  public static boolean printArgNamesMatchError(
      Messager messager, ExecutableElement base, ExecutableElement toCheck) {
    for (int i = 0; i < base.getParameters().size(); i++) {
      Name expectedName = base.getParameters().get(i).getSimpleName();
      VariableElement parameter = toCheck.getParameters().get(i);
      if (!parameter.getSimpleName().contentEquals(expectedName)) {
        messager.printMessage(Diagnostic.Kind.ERROR,
            "Expected this argument to have the name " + expectedName + " to match the argument at "
            + "the same position on "
            + base.getSimpleName(),
            parameter);
        return true;
      }
    }

    return false;
  }

  /**
   * Returns each argument name as it would appear in a signature as the type {@link Object}.
   * Includes comma delimiters if there is more than one argument. Does not include enclosing
   * parenthesis.
   *
   * <p>This method makes each argument {@code final} since only {@code final} arguments can be
   * accessed in an anonymous inner class before JDK 8.
   */
  public static String objectParamList(Iterable<String> argNames) {
    List<String> parameters = new ArrayList<>();
    for (String argName : argNames) {
      parameters.add("final java.lang.Object " + argName);
    }
    return join(", ", parameters);
  }

  /**
   * Returns a compound goal expression, which is either of type {@code "conj"} or {@code "disj"}.
   * If there is only one sub-goal, then it just returns that one goal.
   */
  public static String compoundGoal(String type, List<String> subGoals) {
    if (subGoals.size() == 1) {
      return subGoals.get(0);
    }

    return ClassNames.GOALS + "." + type + "(" + join(", ", subGoals) + ")";
  }

  /**
   * The simple name of a generated implementation class. See the Javadoc of {@link MakeLogicValue}
   * for an example name.
   *
   * @param annotated the annotated type which causes the class to be generated
   */
  public static String generatedClassName(TypeElement annotated) {
    List<Object> components = new ArrayList<>();
    Element element = annotated;
    while (element instanceof TypeElement) {
      components.add(element.getSimpleName());

      element = element.getEnclosingElement();
    }
    Collections.reverse(components);
    return Processors.join("_", components);
  }

  public static boolean isPackageProtected(Element element) {
    return !element.getModifiers().contains(Modifier.PUBLIC)
        && !element.getModifiers().contains(Modifier.PROTECTED)
        && !element.getModifiers().contains(Modifier.PRIVATE);
  }

  private Processors() {}
}
