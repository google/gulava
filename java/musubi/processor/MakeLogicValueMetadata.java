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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;

/**
 * Contains information derived from the {@code MakeLogicValue} annotation.
 */
public final class MakeLogicValueMetadata {
  private final String name;
  private final List<String> fields;

  private MakeLogicValueMetadata(String name, List<String> fields) {
    this.name = name;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
  }

  public String getName() {
    return name;
  }

  public List<String> getFields() {
    return fields;
  }

  /**
   * Returns the metadata stored in a single annotation of type {@code MakeLogicValue}.
   */
  public static MakeLogicValueMetadata forAnnotation(AnnotationMirror annotation) {
    String name = null;
    List<String> fields = null;

    for (ExecutableElement key : annotation.getElementValues().keySet()) {
      AnnotationValue value = annotation.getElementValues().get(key);
      Name keyName = key.getSimpleName();
      if (keyName.contentEquals("name")) {
        name = (String) value.getValue();
      } else if (keyName.contentEquals("fields")) {
        fields = new ArrayList<>();
        for (Object fieldNameValue : (List<?>) value.getValue()) {
          fields.add((String) ((AnnotationValue) fieldNameValue).getValue());
        }
      } else {
        throw new UnsupportedOperationException("Unknown attribute property name: " + keyName);
      }
    }

    return new MakeLogicValueMetadata(name, fields);
  }

  /**
   * Returns all the metadata instances for the annotations on a package.
   */
  public static List<MakeLogicValueMetadata> forPackage(PackageElement pkg) {
    List<MakeLogicValueMetadata> metadatas = new ArrayList<>();

    for (AnnotationMirror annotation : pkg.getAnnotationMirrors()) {
      if (((TypeElement) annotation.getAnnotationType().asElement())
          .getQualifiedName()
          .contentEquals(ClassNames.MAKE_LOGIC_VALUE)) {
        metadatas.add(forAnnotation(annotation));
      }
    }

    return metadatas;
  }
}
