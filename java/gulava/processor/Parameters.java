/*
 *  Copyright (c) 2016 The Gulava Authors
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

import org.pcollections.Empty;
import org.pcollections.PVector;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Represents a list of parameters, including the type and name of each one.
 */
public final class Parameters {
  private static final class Param {
    final String type;
    final String name;

    Param(String type, String name) {
      this.type = type;
      this.name = name;
    }
  }

  private final PVector<Param> params;

  private Parameters(PVector<Param> params) {
    this.params = params;
  }

  /**
   * An instance that represents an empty list.
   */
  public static final Parameters EMPTY = new Parameters(Empty.vector());

  /**
   * Returns an instance that is equivalent to this one but with one more parameter added to the end
   * of the list. This instance is unaffected.
   */
  public Parameters plus(String type, String name) {
    return new Parameters(params.plus(new Param(type, name)));
  }

  /**
   * Returns the number of parameters in this list.
   */
  public int getCount() {
    return params.size();
  }

  /**
   * Names of the arguments of the predicate, separated by commas. This can be used in a method
   * invocation.
   */
  public String getNames() {
    return joinNames(", ");
  }

  /**
   * Joins the values of the parameters in a string concatenation expression. This can be used in
   * Java code in a String concatenation. For example, this may return the string:
   * <pre>
   * {@code a + ", " + b}
   * </pre>
   */
  public String stringExpression() {
    if (params.isEmpty()) {
      return "\"\"";
    }
    return joinNames(" + \", \" + ");
  }

  private String joinNames(String delimiter) {
    List<String> names = new ArrayList<>();
    for (Param param : params) {
      names.add(param.name);
    }
    return Processors.join(delimiter, names);
  }

  /**
   * Returns each argument name as it would appear in a signature. Includes comma delimiters if
   * there is more than one argument. Does not include enclosing parenthesis.
   *
   * <p>This method makes each argument {@code final} since only {@code final} arguments can be
   * accessed in an anonymous inner class before JDK 8.
   */
  @Override
  public String toString() {
    List<String> parameters = new ArrayList<>();
    for (Param param : params) {
      parameters.add(String.format("final %s %s", param.type, param.name));
    }
    return Processors.join(",", parameters);
  }

  /**
   * Returns a list comprised of the parameters of the given method.
   */
  public static Parameters from(ExecutableElement method) {
    Parameters result = EMPTY;
    for (VariableElement parameter : method.getParameters()) {
      result = result.plus(parameter.asType().toString(), parameter.getSimpleName().toString());
    }
    return result;
  }
}
