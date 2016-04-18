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
 * Attach this annotation to an interface or abstract class to automatically create a
 * {@code LogicValue} implementation. The generated {@code LogicValue} implementation adds fields
 * for each abstract method in the interface. The methods must take no arguments, and only be named
 * the name of the field, as opposed to {@code get[FIELD_NAME]}. Each such field method must
 * correspond to a generic type parameter on the interface, which is its return type. This makes
 * pattern matching possible. For instance:
 *
 * <pre>
 * @MakeLogicValue
 * public abstract class PersonName<F, G> {
 *   public abstract F familyName();
 *   public abstract G givenName();
 *
 *   // A static factory method is required for pattern-matching clauses.
 *   public static <F, G> PersonName<F, G> of(F familyName, G givenName) {
 *     return new MakeLogicValue_PersonName<>(familyName, givenName);
 *   }
 * }
 * </pre>
 *
 * The name of the generated type is several components joined by an underscore:
 * {@code MakeLogicValue}, the enclosing classes of the annotated class, and the annotated class
 * name itself. For instance, for
 *
 * <pre>
 * class OuterClass {
 *   @MakeLogicValue
 *   static class InnerClass {...}
 * }
 * </pre>
 *
 * <p>The generated class will be named {@code MakeLogicValue_OuterClass_InnerClass}.
 *
 * <p>Generated logic value types are also generic. These fields are completely unbound, meaning
 * they can be {@link Object} or {@code Var} or some other implementation of {@code LogicValue}. If
 * you don't care about the generic type parameters (perhaps because you will not call any methods
 * on the fields), you can just use an unqualified reference, although you should use the diamond
 * syntax to invoke the constructor to avoid compiler warnings, e.g.
 * {@code MyValue v = new MyValue<>("x", "y")}.
 *
 * <p>Note that to suppress a rawtypes warning for {@code MyValue v} (these are shown by default in
 * Eclipse), you should qualify it as {@code MyValue<?, ?> v}.
 */
@Target(ElementType.TYPE)
public @interface MakeLogicValue {}
