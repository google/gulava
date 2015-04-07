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

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Utility code for implementing annotation processors.
 */
public class Processors {
  /**
   * Reports an exception as an error to the given messager. This includes printing the stacktrace
   * to stderr.
   *
   * @param messager the messager to report the error to
   * @param throwable the exception to report
   * @param element the element which the exception is related to
   */
  public static void print(Messager messager, Throwable throwable, Element element) {
    messager.printMessage(Diagnostic.Kind.ERROR, throwable.toString(), element);
    throwable.printStackTrace();
  }

  /**
   * Joins the string representation of each object in {@code objects} into a single string.
   * {@code delimiter} is used to separate the objects, but is not added before the first or after
   * the last object.
   */
  public static String join(String delimiter, Iterable<?> objects) {
    StringBuilder list = new StringBuilder();
    boolean first = true;
    for (Object object : objects) {
      if (!first) {
        list.append(delimiter);
      }
      first = false;
      list.append(object);
    }
    return list.toString();
  }

  private Processors() {}
}
