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
package gulava;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Utilities for text-based, tree-shaped object dumping.
 */
public final class Dumper implements Flushable {
  private final int indentation;
  private final Writer writer;

  /**
   * @param indentation the indentation of the heading, as number of spaces. All sub-elements are
   *     indented relative to this level
   * @param writer where to dump to
   */
  public Dumper(int indentation, Writer writer) {
    this.indentation = indentation;
    this.writer = writer;
  }

  /**
   * Returns a new instance that indents one level deeper. Calling this iteratively has accumulative
   * effects (e.g. {@code d.plusIndentation().plusIndentation.plusIndentation()}, but never affects
   * an already-created instance.
   */
  private Dumper plusIndentation() {
    return new Dumper(indentation + 2, writer);
  }

  /**
   * Dumps a tree of objects. Usually, an implementation of {@link Dumpable#dump(Dumper)} can
   * just delegate to this method.
   *
   * @param heading the text that appears above all the information in the dump
   * @param elements the elements to include in the dump, in order. Each item will be passed to
   *     {@link #dump(Object)}.
   */
  public void dump(String heading, Object... elements) throws IOException {
    indent();
    writer.write(heading);
    writer.write('\n');

    Dumper deeper = plusIndentation();
    for (Object element : elements) {
      deeper.dump(element);
    }
  }

  /**
   * Dumps a single object at this dumper's indentation level.
   *
   * @param what the object to dump. This can be a {@link Dumpable}, {@code null}, or any other
   *     object. If {@code null} or a non-{@link Dumpable}, the object is converted to a String with
   *     {@link String#valueOf(Object)}.
   */
  public void dump(Object what) throws IOException {
    if (what instanceof Dumpable) {
      Dumpable dumpable = (Dumpable) what;
      ArrayList<Object> subcomponents = new ArrayList<>();
      dumpable.addSubcomponents(subcomponents);
      dump(dumpable.dumpHeading(), subcomponents.toArray());
    } else {
      indent();
      writer.write(String.valueOf(what));
      writer.write("\n");
    }
  }

  /**
   * Flushes the underlying writer.
   */
  @Override
  public void flush() throws IOException {
    writer.flush();
  }

  private void indent() throws IOException {
    for (int i = 0; i < indentation; i++) {
      writer.write(' ');
    }
  }
}
