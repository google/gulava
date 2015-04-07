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
package musubi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Attach this annotation to an interface to automatically create a {@code LogicValue}
 * implementation. The generated {@code LogicValue} implementation adds fields for each abstract
 * method in the interface. The methods must return {@link Object}, take no arguments, and only be
 * named the name of the field, as opposed to {@code get[FIELD_NAME]}.
 *
 * <p>Generated logic value types are generic, with a single type parameter for each field. These
 * fields are completely unbound, meaning they can be {@link Object} or {@code Var} or some other
 * implementation of {@code LogicValue}. If you don't care about the generic type parameters
 * (perharps because you will not call any methods on the fields), you can just use an unqualified
 * reference, although you should use the diamond syntax to invoke the constructor to avoid compiler
 * warnings, e.g. {@code MyValue v = new MyValue<>("x", "y")}.
 */
@Target(ElementType.TYPE)
public @interface MakeLogicValue {
  /**
   * The name of the implementation type to generate.
   */
  String name();
}
