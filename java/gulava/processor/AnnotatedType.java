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
package gulava.processor;

import gulava.annotation.CollectErrors;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Represents a {@link TypeElement} that is annotated with some particular annotation.
 */
public final class AnnotatedType {
  private final TypeElement type;
  private final PackageElement pkg;
  private final ProcessingEnvironment env;

  /** The annotated type. */
  public TypeElement getType() {
    return type;
  }

  /** The package containing the annotated type. */
  public PackageElement getPkg() {
    return pkg;
  }

  /**
   * Opens a new writer to generate a class in the same package as the annotated type. This
   * automatically adds a {@code package} line.
   */
  public ClassWriter openWriter(String generatedClassName) throws IOException {
    if (generateErrorClass()) {
      return new ClassWriter(new StringWriter(), generatedClassName);
    } else {
      return new ClassWriter(env, pkg, generatedClassName);
    }
  }

  private boolean generateErrorClass() {
    return type.getAnnotation(CollectErrors.class) != null;
  }

  /**
   * Returns a new instance based on the given type, which is assumed to be annotated.
   */
  private AnnotatedType(TypeElement type, ProcessingEnvironment env) {
    Element packageElement = type.getEnclosingElement();
    while (!(packageElement instanceof PackageElement)) {
      packageElement = packageElement.getEnclosingElement();
    }
    this.pkg = (PackageElement) packageElement;
    this.type = type;
    this.env = env;
  }

  public static abstract class Processor extends AbstractProcessor {
    /**
     * @param annotatedType the type being processed
     * @param messager the messager that should be used when generating code from this annotated
     *     type
     */
    protected abstract void process(AnnotatedType annotatedType, Messager messager)
        throws IOException;

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      for (TypeElement annotation : annotations) {
        Iterable<? extends TypeElement> types =
            ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation));
        for (TypeElement type : types) {
          AnnotatedType annotatedType = new AnnotatedType(type, processingEnv);
          String errorsClassName = Processors.generatedClassName(type) + "_Errors";
          try {
            ClassWriter errorsClassWriter = annotatedType.generateErrorClass()
                ? new ClassWriter(processingEnv, annotatedType.getPkg(), errorsClassName)
                : new ClassWriter(new StringWriter(), errorsClassName);

            try (ClassGeneratingMessager classGeneratingMessager =
                new ClassGeneratingMessager(errorsClassWriter)) {
              Messager messager = annotatedType.generateErrorClass()
                  ? classGeneratingMessager : processingEnv.getMessager();
              process(annotatedType, messager);
            }
          } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString(), type);
          }
        }
      }
      return true;
    }
  }
}
