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
package gulava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Attach this annotation to a class which contains methods defining a goal.
 *
 * <p>Every static, non-private method in the annotated class which returns a {@code Goal} is
 * combined with {@code disj} into a single {@link Goal} which is returned by the generated goal
 * factory. Each such method should have the same number of arguments.
 *
 * <p>The goal factory is a class with three static methods: {@code i}, {@code o}, and {@code d}.
 * Each one returns the combined goal with slightly different behavior. {@code i} stands for inline
 * and indicates that the result of {@code disj} is returned as-is. {@code o} is like {@code i} but
 * returns a goal that only calls {@code disj} when run. This is usually a good method to use in
 * recursive definitions. {@code d} stands for delayed and is similar to {@code o} but returns an
 * immature stream. This method is useful when using {@code o} causes convergence for certain
 * unbound arguments.
 *
 * <p>All three of the above methods are available for use by the methods of the annotated class.
 */
@Target(ElementType.TYPE)
public @interface MakeGoalFactory {
  /**
   * The name of the generated goal factory class. This is generated in the same package as the
   * class with this annotation.
   */
  String name();
}
