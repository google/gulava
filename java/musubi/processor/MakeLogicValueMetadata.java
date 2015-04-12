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

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Contains information derived from a {@code @MakeLogicValue} annotation and the type that it
 * annotates.
 */
public final class MakeLogicValueMetadata {
  private final String name;
  private final List<LogicValueField> fields;
  private final TypeElement interfaze;
  private final boolean autoDefineToString;

  private MakeLogicValueMetadata(String name, List<LogicValueField> fields, TypeElement interfaze,
      boolean autoDefineToString) {
    this.name = name;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
    this.interfaze = interfaze;
    this.autoDefineToString = autoDefineToString;
  }

  public String getName() {
    return name;
  }

  public List<LogicValueField> getFields() {
    return fields;
  }

  public TypeElement getInterface() {
    return interfaze;
  }

  public boolean autoDefineToString() {
    return autoDefineToString;
  }

  /**
   * Returns the type parameters of each field, separated by commas, and enclosed in alligator
   * brackets, e.g. {@code "<FOO_TYPE, BAR_TYPE>"}.
   */
  public String typeParametersAlligator() {
    if (fields.isEmpty()) {
      return "";
    }
    List<String> typeParameters = new ArrayList<>();
    for (LogicValueField field : fields) {
      typeParameters.add(field.getTypeParameter());
    }
    return "<" + Processors.join(", ", typeParameters) + ">";
  }

  /**
   * Generates an instantiation expression given a list of arguments to pass to the constructor.
   */
  public String instantiation(Iterable<String> arguments) {
    return String.format("new %s%s(%s)",
        name,
        fields.isEmpty() ? "" : "<>",
        Processors.join(", ", arguments));
  }

  /**
   * The simple name of the generated {@code LogicValue} implementation class. See the Javadoc of
   * {@link MakeLogicValue}.
   */
  private static String generatedClassName(TypeElement interfaze) {
    List<Object> components = new ArrayList<>();
    Element element = interfaze;
    while (element instanceof TypeElement) {
      components.add(element.getSimpleName());

      element = element.getEnclosingElement();
    }
    components.add("MakeLogicValue");
    Collections.reverse(components);
    return Processors.join("_", components);
  }

  /**
   * Returns the metadata stored in a single annotation of type {@code MakeLogicValue}.
   */
  public static MakeLogicValueMetadata forInterface(TypeElement interfaze, Messager messager) {
    MakeLogicValue annotation = interfaze.getAnnotation(MakeLogicValue.class);
    List<LogicValueField> fields = new ArrayList<>();
    boolean autoDefineToString = true;

    for (ExecutableElement method : ElementFilter.methodsIn(interfaze.getEnclosedElements())) {
      if (method.getSimpleName().contentEquals("toString")) {
        autoDefineToString = false;
      } else if (!method.getModifiers().contains(Modifier.STATIC)) {
        fields.add(new LogicValueField(method.getSimpleName().toString()));
      }
    }

    String name = generatedClassName(interfaze);
    int typeParameterCount = interfaze.getTypeParameters().size();

    if (typeParameterCount != fields.size()) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Expect one generic type parameter for each field. There are "
          + fields.size() + " field(s) and "
          + typeParameterCount + " type parameter(s).", interfaze);
    }

    return new MakeLogicValueMetadata(name, fields, interfaze, autoDefineToString);
  }
}
