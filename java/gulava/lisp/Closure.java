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
package gulava.lisp;

import gulava.annotation.MakeLogicValue;

/**
 * A closure in an interpreted Lisp program. A closure is an expression and an environment in which
 * that expression sould be evaluated. Note, however, that whenever a closure is invoked as a
 * function, any number of items may be pushed onto the environment when passed as arguments.
 * Because the expression uses {@link StackRef} values to refer to values in the environment, the
 * environment values do not have names, hence arguments to closures do not have names. In fact,
 * closures do not even have arities. See {@link Lisp} for more information.
 */
@MakeLogicValue
public abstract class Closure<X, E> {
  /**
   * The lisp expression to evaluate.
   */
  public abstract X exp();

  /**
   * The <em>base</em> environment at which to evaluate the expression when the closure is invoked.
   * If the closure is invoked with zero arguments, the base environment is the actual environment
   * at which the closure's expression is evaluated. Otherwise, each argument is pushed onto the
   * base environment stack. The first argument is pushed first, and the last argument is pushed
   * last, which means the last argument becomes the {@code car} of the new environment.
   */
  public abstract E env();

  public static <X, E> Closure<X, E> of(X exp, E env) {
    return new MakeLogicValue_Closure<>(exp, env);
  }
}
