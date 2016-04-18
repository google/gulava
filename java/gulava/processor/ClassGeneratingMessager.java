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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * A messager which writes the messages to a class so they can be retrieved later programmatically.
 * This generates a class with a static method called {@code add} which accepts a
 * {@code List<? super String>} and adds all errors that were reported.
 */
public final class ClassGeneratingMessager implements Messager, Closeable {
  private final List<String> messages = new ArrayList<>();
  private final ClassWriter writer;

  public ClassGeneratingMessager(ClassWriter writer) throws IOException {
    this.writer = writer;
  }

  @Override
  public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
    messages.add(kind + ":" + msg + ":");
  }

  @Override
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
    messages.add(kind + ":" + msg + ":" + e.getSimpleName() + ":");
  }

  @Override
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
    printMessage(kind, msg, e);
  }

  @Override
  public void printMessage(
      Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
    printMessage(kind, msg, e);
  }

  /**
   * Writes all messages to and closes the underlying {@link ClassWriter}.
   */
  @Override
  public void close() throws IOException {
    writer.write("public class " + writer.getClassName() + " {\n");
    writer.write("  public static void add("
        + "java.util.List<? super java.lang.String> destination) {\n");
    for (String message : messages) {
      writer.write("    destination.add(\"" + message + "\");\n");
    }
    messages.clear();
    writer.write("  }\n");
    writer.write("}\n");
    writer.close();
  }
}
