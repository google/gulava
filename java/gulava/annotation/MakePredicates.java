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
 * Attach to an abstract class to automatically implement {@code Goal}-returning predicate methods.
 *
 * <p>A class with this annotation contains two kinds of methods: predicate methods and clauses. All
 * these methods are essentially {@code Goal} factory methods. Predicate methods return a compound
 * {@code Goal} that is a {@code disj} of the {@code Goal}s returned by corresponding clause
 * methods. Each predicate method is {@code abstract} and is associated with one or more
 * clause methods. For example:
 *
 * import gulava.annotation.MakePredicates;
 * import gulava.Goal;
 *
 * <pre>
 * @MakePredicates
 * abstract class Lists {
 *   // Appends two lists - a and b - to make c
 *   public abstract Goal append(Object a, Object b, Object c);
 *
 *   final Goal append_baseCase(Void a, Object b, Object c) {
 *     return same(b, c);
 *   }
 *
 *   final Goal append_iterate(Cons<?, ?> a, Object b, Cons<?, ?> c) {
 *     return conj(
 *         same(a.car(), c.car()),
 *         append(a.cdr(), b, c.cdr()));
 *   }
 *
 *   // Indicates that 'element' is contained in the sequence 'list'
 *   public abstract Goal member(Object element, Object list);
 *
 *   final Goal member_pass(Object element, Cons<?, ?> list) {
 *     return member(element, list.cdr());
 *   }
 *
 *   final Goal member_select(Object element, Cons<?, ?> list) {
 *     return same(element, list.car());
 *   }
 *
 *   // This is generally a good idea so that clients do not have to care about how the goals are
 *   // implemented.
 *   public static final INSTANCE = new MakePredicates_Lists();
 * }
 * </pre>
 *
 * <p>This code implements two predicates common in logic programs that operate on sequences. Note
 * the following requirements:
 * <ul>
 *   <li>Predicate methods are abstract
 *   <li>Clause methods are {@code final} and package-protected
 *   <li>Clause methods are named in the form [PREDICATE-NAME]_[DESCRIPTION]
 *   <li>Javadoc generally should appear only on the predicate methods
 *   <li>The number and names of parameters should be the same between the predicate method and all
 *       its corresponding clauses. The types of the parameters need not match.
 *   <li>The types of the parameters on the predicate methods should all be {@link Object}
 * <ul>
 * When clause methods have parameter types other than {@link Object}, it means that the value will
 * be bound to a type as a condition of the clause being checked. {@link Void} indicates
 * {@code null} is required for some value. Generic types like {@code Cons<?, ?>} are allowed, but
 * can also be nested arbitrarily, as in {@code Cons<?, Cons<?, ?>>}, which would indicate a
 * sequence of length 2 or more.
 */
@Target(ElementType.TYPE)
public @interface MakePredicates {}
