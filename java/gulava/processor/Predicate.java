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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;

/**
 * Contains metadata about a particular predicate. This includes the predicate method and the
 *  method for each clause.
 */
public final class Predicate {
  private final ExecutableElement method;
  private final List<ExecutableElement> clauses;

  public Predicate(ExecutableElement method, List<ExecutableElement> clauses) {
    this.method = method;
    this.clauses = Collections.unmodifiableList(new ArrayList<>(clauses));
  }

  public ExecutableElement getMethod() {
    return method;
  }

  /**
   * The name of the predicate. This is derived from the method name.
   */
  public Name getName() {
    return method.getSimpleName();
  }

  /**
   * The names of the parameters to the predicate. These names are shared between the predicate
   * method and all clause methods.
   */
  public Parameters getParameters() {
    return Parameters.from(method);
  }

  public List<ExecutableElement> getClauses() {
    return clauses;
  }
}
