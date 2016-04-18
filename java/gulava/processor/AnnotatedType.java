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
package gulava.processor;

import gulava.annotation.CollectErrors;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * Represents a {@link TypeElement} that is annotated with some particular annotation.
 */
public final class AnnotatedType {
  private final ProcessingEnvironment env;
  private final TypeElement type;
  private final PackageElement pkg;
  private final ClassGeneratingMessager classGeneratingMessager;

  private AnnotatedType(ProcessingEnvironment env, TypeElement type, PackageElement pkg,
      ClassGeneratingMessager classGeneratingMessager) {
    this.env = env;
    this.type = type;
    this.pkg = pkg;
    this.classGeneratingMessager = classGeneratingMessager;
  }

  /** The annotated type. */
  public TypeElement getType() {
    return type;
  }

  /** The package containing the annotated type. */
  public PackageElement getPkg() {
    return pkg;
  }

  /**
   * The messager that should be used when generating code from this annotated type.
   */
  public Messager getMessager() {
    if (classGeneratingMessager != null) {
      return classGeneratingMessager;
    }
    return env.getMessager();
  }

  /**
   * Saves the errors that have been generated using the messager of this object. This is a no-op
   * unless running in {@code @CollectErrors} mode.
   */
  public void saveErrors() throws IOException {
    if (classGeneratingMessager != null) {
      classGeneratingMessager.close();
    }
  }

  /**
   * Opens a new writer to generate a class in the same package as the annotated type. This
   * automatically adds a {@code package} line.
   */
  public ClassWriter openWriter(String generatedClassName) throws IOException {
    if (classGeneratingMessager != null) {
      return new ClassWriter(new StringWriter(), generatedClassName);
    } else {
      return new ClassWriter(env, pkg, generatedClassName);
    }
  }

  /**
   * Returns a new instance based on the given type, which is assumed to be annotated.
   */
  public static AnnotatedType of(TypeElement type, ProcessingEnvironment env) throws IOException {
    Element packageElement = type.getEnclosingElement();
    while (!(packageElement instanceof PackageElement)) {
      packageElement = packageElement.getEnclosingElement();
    }
    PackageElement pkg = (PackageElement) packageElement;

    ClassGeneratingMessager classGeneratingMessager = null;
    if (type.getAnnotation(CollectErrors.class) != null) {
      String className = Processors.generatedClassName(type) + "_Errors";
      classGeneratingMessager = new ClassGeneratingMessager(new ClassWriter(env, pkg, className));
    }

    return new AnnotatedType(env, type, pkg, classGeneratingMessager);
  }

  /**
   * Returns a list representing each type that is annotated with an annotation in
   * {@code annotations}.
   */
  public static List<AnnotatedType> all(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv,
      ProcessingEnvironment env) {
    List<AnnotatedType> all = new ArrayList<>();

    for (TypeElement annotation : annotations) {
      Iterable<? extends TypeElement> types =
          ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation));
      for (TypeElement type : types) {
        try {
          all.add(of(type, env));
        } catch (IOException e) {
          Processors.print(env.getMessager(), e, type);
        }
      }
    }

    return all;
  }
}
