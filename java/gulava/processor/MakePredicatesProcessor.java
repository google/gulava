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

import gulava.annotation.MakePredicates;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * An annotation processor that reads classes annoated with @{@link MakePredicates} and creates
 * a in implementing class for each annotated type.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_PREDICATES)
public final class MakePredicatesProcessor extends AbstractProcessor {
  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (AnnotatedType annotatedType : AnnotatedType.all(annotations, roundEnv)) {
      MakePredicatesMetadata metadata =
          MakePredicatesMetadata.of(annotatedType.getType(), processingEnv.getMessager());
      try (Writer writer = annotatedType.openWriter(processingEnv, metadata.getName())) {
        writer.write("public class " + metadata.getName()
            + " extends " + metadata.getAnnotatedType().getQualifiedName()
            + " {\n");

        for (Predicate predicate : metadata.getPredicates()) {
          GoalExpressions expressions = new GoalExpressions("this", processingEnv.getMessager());
          String inlineName = "__" + predicate.getName() + "Inline__";
          expressions.writeInlineMethod(writer, "private", inlineName, predicate.getClauses());
          writer.write("\n");
          writer.write("  @java.lang.Override\n");
          writer.write("  public " + ClassNames.GOAL + " " + predicate.getName() + "("
              + Processors.objectParamList(predicate.argNames()) + ") {\n");
          writer.write("    return new " + ClassNames.GOAL + "() {\n");
          writer.write("      @java.lang.Override\n");
          writer.write("      public " + ClassNames.STREAM + " run("
              + ClassNames.SUBST + " __subst__) {\n");
          writer.write("        return " + metadata.getName() + ".this." + inlineName + "("
              + Processors.join(", ", predicate.argNames()) + ").run(__subst__);\n");
          writer.write("      }\n");
          writer.write("    };\n");
          writer.write("  }\n");
        }

        writer.write("}\n");
      } catch (IOException e) {
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.ERROR, e.toString(), annotatedType.getType());
        e.printStackTrace();
      }
    }
    return true;
  }
}
