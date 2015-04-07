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

import java.util.Locale;

/**
 * Information about a field in a generated logic value.
 */
public final class LogicValueField {
  private final String name;

  public LogicValueField(String name) {
    this.name = name;
  }

  /**
   * The name of the field.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the type parameter.
   */
  public String getTypeParameter() {
    return name.toUpperCase(Locale.US) + "_TYPE";
  }

  /**
   * The name of the setter method in the generated value builder.
   */
  public String getSetterMethodName() {
    return "set" + Processors.capitalizeFirst(name);
  }

  /**
   * Returns the type parameter and name separate by a space - useable as a kind of declaration or
   * in a method signature.
   */
  public String getTypeAndName() {
    return getTypeParameter() + " " + getName();
  }

  @Override
  public String toString() {
    return name;
  }
}
