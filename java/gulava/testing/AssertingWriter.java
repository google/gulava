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
package gulava.testing;

import org.junit.Assert;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;

public final class AssertingWriter extends FilterWriter {
  int flushes;

  public AssertingWriter() {
    super(new StringWriter());
  }

  @Override
  public void flush() throws IOException {
    out.flush();
    flushes++;
  }

  public void assertLines(String... expectedLines) {
    StringBuilder expected = new StringBuilder();
    for (String line : expectedLines) {
      expected.append(line);
      expected.append('\n');
    }
    Assert.assertEquals(expected.toString(), out.toString());
  }

  public void assertFlushes(int expected) {
    Assert.assertEquals(expected, flushes);
  }
}
