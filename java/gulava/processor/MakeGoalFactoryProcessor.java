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

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Messager;
import javax.annotation.processing.SupportedAnnotationTypes;

/**
 * An annotation processor that reads classes annoated with @{@code MakeGoalFactory} and creates
 * a corresponding class for each one that constructs a certain goal.
 */
@SupportedAnnotationTypes(ClassNames.MAKE_GOAL_FACTORY)
public final class MakeGoalFactoryProcessor extends AnnotatedType.Processor {
  /**
   * Generates a goal factory class.
   */
  @Override
  protected void process(AnnotatedType annotatedType, Messager messager) throws IOException {
    MakeGoalFactoryMetadata metadata =
        MakeGoalFactoryMetadata.of(annotatedType.getType(), messager);
    try (Writer writer = annotatedType.openWriter(metadata.getName())) {
      writer.write("public class " + metadata.getName() + " {\n");

      // Goal factory method: i (inline)
      GoalExpressions expressions = new GoalExpressions(
          metadata.getAnnotatedType().getQualifiedName().toString(),
          processingEnv.getMessager());
      expressions.writeInlineMethod(
          writer, "public static", "i", metadata.getClauseMethods(), metadata.getParameters());

      // Goal factory method: o (normal)
      writer.write("  public static " + ClassNames.GOAL + " o("
          + metadata.getParameters() + ") {\n");
      writer.write("    return new " + ClassNames.GOAL + "() {\n");
      writer.write("      @java.lang.Override\n");
      writer.write("      public " + ClassNames.STREAM + " run("
          + ClassNames.SUBST + " __subst__) {\n");
      writer.write("        return i("
          + metadata.getParameters().getNames() + ").run(__subst__);\n");
      writer.write("      }\n");
      writer.write("    };\n");
      writer.write("  }\n");

      // Goal factory method: d (delayed)
      writer.write("  public static " + ClassNames.GOAL + " d("
          + metadata.getParameters() + ") {\n");
      writer.write("    return new " + ClassNames.DELAYED_GOAL
          + "(o(" + metadata.getParameters().getNames() + "));\n");
      writer.write("  }\n");

      writer.write("}\n");
    }
  }
}
