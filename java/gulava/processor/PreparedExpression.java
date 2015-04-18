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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Java code expression, where the expression is a single {@link String} but also
 * requires some preparation code to be inserted before the expression is inserted. The preparation
 * code may be used, for instance, to initialize a value that must be passed multiple times to
 * methods in the expression.
 */
public final class PreparedExpression {
  private final String expression;
  private final List<String> preparationStatements;

  public PreparedExpression(String expression, List<String> preparationStatements) {
    this.expression = expression;
    this.preparationStatements =
        Collections.unmodifiableList(new ArrayList<>(preparationStatements));
  }

  /** The actual expression that can be inserted into code after the preparation statements. */
  public String getExpression() {
    return expression;
  }

  /** The preparation statements that should appear before the expression, if any. */
  public List<String> getPreparationStatements() {
    return preparationStatements;
  }

  /** Writes the lines, each holding a statement, that should appear before the expression. */
  public void writePreparationStatements(Writer writer) throws IOException {
    for (String preparationStatement : preparationStatements) {
      writer.write(preparationStatement);
    }
  }
}
