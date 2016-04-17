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

import gulava.annotation.MakeLogicValue;

import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Returns true if clauses should pass through a value of this type unchanged to individual
 * predicates. These are generally types that are not logic value types.
 */
public final class IsPassThroughType extends SimpleTypeVisitor6<Boolean, Void> {
  public IsPassThroughType() {
    super(/*defaultValue=*/false);
  }

  @Override
  public Boolean visitPrimitive(PrimitiveType type, Void v) {
    return true;
  }

  @Override
  public Boolean visitDeclared(DeclaredType type, Void v) {
    if (Processors.qualifiedName(type).contentEquals("java.lang.Void")) {
      return false;
    }

    // This could be better. Not all logic values use the @MakeLogicValue annotation. We may have to
    // check implemented interfaces as well.
    if (type.asElement().getAnnotation(MakeLogicValue.class) == null) {
      return true;
    }

    for (TypeMirror typeArgument : type.getTypeArguments()) {
      if (visit(typeArgument)) {
        return true;
      }
    }
    return false;
  }
}
