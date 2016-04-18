/*
 *  Copyright (c) The Gulava Authors
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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;

/**
 * A writer which is used for generating code for a class or interface.
 */
public final class ClassWriter extends FilterWriter {
  private final String className;

  /**
   * Creates an instance that writes to the given underlying writer.
   */
  public ClassWriter(Writer writer, String className) {
    super(writer);
    this.className = className;
  }

  /**
   * Creates an instance for generating a class of the given name in the given package.
   * This automatically generates a {@code package} line.
   */
  public ClassWriter(ProcessingEnvironment env, PackageElement pkg, String className)
      throws IOException {
    this(env.getFiler()
            .createSourceFile(pkg.getQualifiedName() + "." + className)
            .openWriter(),
         className);
    write("package " + pkg.getQualifiedName() + ";\n");
    write("\n");
  }

  /**
   * Returns the unqualified name of the class it is generating.
   */
  public String getClassName() {
    return className;
  }
}
