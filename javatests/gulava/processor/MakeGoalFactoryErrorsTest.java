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

import gulava.Goal;
import gulava.annotation.CollectErrors;
import gulava.annotation.MakeGoalFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class MakeGoalFactoryErrorsTest {
  @MakeGoalFactory(name = "Unused")
  @CollectErrors
  static class ArgCountMismatch {
    static Goal foo(Object x) {
      return null;
    }

    static Goal bar(Object x, Object y) {
      return null;
    }
  }

  private void assertRegex(String regex, String s) {
    Assert.assertTrue(s, s.matches(regex));
  }

  @Test
  public void argCountMismatch() {
    List<String> dest = new ArrayList<>();
    MakeGoalFactoryErrorsTest_ArgCountMismatch_Errors.add(dest);
    Assert.assertEquals(1, dest.size());
    assertRegex(
        "ERROR:Expected this method to have 1 parameter[(]s[)] to match foo but it has: 2:bar:.*",
        dest.get(0));
  }

  @MakeGoalFactory(name = "")
  @CollectErrors
  static class EmptyName {
    static Goal foo(Object x) {
      return null;
    }
  }

  @Test
  public void emptyName() {
    List<String> dest = new ArrayList<>();
    MakeGoalFactoryErrorsTest_EmptyName_Errors.add(dest);
    Assert.assertEquals(1, dest.size());
    assertRegex(
        "ERROR:Require non-empty name in @MakeGoalFactory annotation[.]:EmptyName:.*",
        dest.get(0));
  }

  @MakeGoalFactory(name = "Unused")
  @CollectErrors
  static class NoClauses {
    private static void foo(Object x) {}
    public void bar(Object x) {}
  }

  @Test
  public void noClauses() {
    List<String> dest = new ArrayList<>();
    MakeGoalFactoryErrorsTest_NoClauses_Errors.add(dest);
    Assert.assertEquals(1, dest.size());
    assertRegex("ERROR:"
        + "Expect at least one static, non-private method in class annotated with @MakeGoalFactory:"
        + "NoClauses:.*",
        dest.get(0));
  }

  @MakeGoalFactory(name = "Unused")
  @CollectErrors
  static class MismatchArgNames {
    static Goal foo(Object matches, Object x) {
      return null;
    }

    static Goal bar(Object matches, Object y) {
      return null;
    }
  }

  @Test
  public void mismatchArgNames() {
    List<String> dest = new ArrayList<>();
    MakeGoalFactoryErrorsTest_MismatchArgNames_Errors.add(dest);
    Assert.assertEquals(1, dest.size());
    assertRegex("ERROR:"
        + "Expected this argument to have the name x to match the argument at the same position on "
        + "foo:y:.*",
        dest.get(0));
  }
}
