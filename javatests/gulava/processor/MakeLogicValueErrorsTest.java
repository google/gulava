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

import gulava.annotation.CollectErrors;
import gulava.annotation.MakeLogicValue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class MakeLogicValueErrorsTest {
  @MakeLogicValue
  @CollectErrors
  abstract static class WrongGenericArity<X> {
    public abstract X field1();
    public abstract X field2();
  }

  private void assertRegex(String regex, String s) {
    Assert.assertTrue(s, s.matches(regex));
  }

  @Test
  public void wrongGenericArity() {
    List<String> dest = new ArrayList<>();
    MakeLogicValueErrorsTest_WrongGenericArity_Errors.add(dest);
    Assert.assertEquals(1, dest.size());
    assertRegex("ERROR:Expect one generic type parameter for each field. There are 2 field[(]s[)]"
        + " and 1 type parameter[(]s[)][.]:WrongGenericArity:.*",
        dest.get(0));
  }
}
