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

import java.util.Collection;

/**
 * An object that can be dumped using the functionality in {@link Dumper} with output that is richer
 * than {@link Object#toString()}. While overriding {@code toString()} can give reasonable results
 * if the object to represent has no structure, no subfields, and has a single-line String
 * representation, you should implement this interface in order to recursively dump subfields and
 * get readable output. This interface is invoked implicitly by
 * {@link Dumper#dump(String, Object...)} and {@link Dumper#dump(Object)}.
 */
public interface Dumpable {
  /**
   * The heading this object has in a dump.
   */
  String dumpHeading();

  /**
   * Adds the objects to {@code destination} that appear nested in this object in the dump.
   */
  void addSubcomponents(Collection<Object> destination);
}
