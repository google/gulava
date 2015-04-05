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

import musubi.annotation.MakeLogicValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * Contains information derived from the {@code MakeLogicValue} annotation.
 */
public final class MakeLogicValueMetadata {
  private final String name;
  private final List<String> fields;
  private final TypeElement interfaze;

  private MakeLogicValueMetadata(String name, List<String> fields, TypeElement interfaze) {
    this.name = name;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
    this.interfaze = interfaze;
  }

  public String getName() {
    return name;
  }

  public List<String> getFields() {
    return fields;
  }

  public TypeElement getInterface() {
    return interfaze;
  }

  /**
   * Returns the metadata stored in a single annotation of type {@code MakeLogicValue}.
   */
  public static MakeLogicValueMetadata forInterface(TypeElement interfaze) {
    MakeLogicValue annotation = interfaze.getAnnotation(MakeLogicValue.class);
    List<String> fields = new ArrayList<>();

    for (ExecutableElement method : ElementFilter.methodsIn(interfaze.getEnclosedElements())) {
      fields.add(method.getSimpleName().toString());
    }

    String name = annotation.name();
    if (name == null) {
      throw new IllegalArgumentException(
          "No name given for MakeLogicValue annotation on: " + interfaze.getQualifiedName());
    }

    return new MakeLogicValueMetadata(name, fields, interfaze);
  }
}
