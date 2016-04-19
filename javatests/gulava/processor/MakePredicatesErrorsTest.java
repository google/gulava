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
import gulava.annotation.MakePredicates;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class MakePredicatesErrorsTest {
  @MakePredicates
  @CollectErrors
  public abstract static class ClauseWithoutPredicate {
    final Goal thePredicate_foo(Object x) {
      return null;
    }
  }

  private void assertRegex(String regex, String s) {
    Assert.assertTrue(s, s.matches(regex));
  }

  @Test
  public void clauseWithoutPredicate() {
    List<String> errors = new ArrayList<>();
    MakePredicatesErrorsTest_ClauseWithoutPredicate_Errors.add(errors);
    Assert.assertEquals(1, errors.size());
    assertRegex("ERROR:"
        + "Clause method without predicate method. Expect an abstract method with signature"
        + " of: thePredicate[(]java[.]lang[.]Object x[)]:thePredicate_foo:.*",
        errors.get(0));
  }

  @MakePredicates
  @CollectErrors
  public abstract static class NoClausesForPredicate {
    public abstract Goal thePredicate(Object x);

    // anotherPredicate/1 is missing clauses but anotherPredicate/2 is OK.

    public abstract Goal anotherPredicate(Object x);

    public abstract Goal anotherPredicate(Object x, Object y);

    final Goal anotherPredicate_foo(Object x, Object y) {
      return null;
    }
  }

  @Test
  public void noClausesForPredicate() {
    List<String> errors = new ArrayList<>();
    MakePredicatesErrorsTest_NoClausesForPredicate_Errors.add(errors);
    Assert.assertEquals(2, errors.size());
    assertRegex("ERROR:No clauses found for predicate[.]:thePredicate:.*", errors.get(0));
    assertRegex("ERROR:No clauses found for predicate[.]:anotherPredicate:.*", errors.get(1));
  }

  @MakePredicates
  @CollectErrors
  public abstract static class MissingClausesForOverloadsOnNonLogicTypes {
    public abstract Goal isOverloadedByPassThrough(int x, Object y);

    public abstract Goal isOverloadedByPassThrough(String x, Object y);
  }

  @Test
  public void missingClausesForOverloadsOnNonLogicTypes() {
    List<String> errors = new ArrayList<>();
    MakePredicatesErrorsTest_MissingClausesForOverloadsOnNonLogicTypes_Errors.add(errors);
    Assert.assertEquals(2, errors.size());
    assertRegex("ERROR:No clauses found for predicate.:isOverloadedByPassThrough:.*",
        errors.get(0));
    assertRegex("ERROR:No clauses found for predicate.:isOverloadedByPassThrough:.*",
        errors.get(1));
  }
}
