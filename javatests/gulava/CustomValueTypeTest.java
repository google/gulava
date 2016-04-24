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

import static gulava.Goals.conj;
import static gulava.Goals.same;

import gulava.Cons;
import gulava.LogicValue;
import gulava.Subst;
import gulava.Var;
import gulava.testing.LogicAsserter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class CustomValueTypeTest {
  static final class PersonName implements LogicValue {
    private final Object familyName;
    private final Object givenName;

    PersonName(Object familyName, Object givenName) {
      this.familyName = familyName;
      this.givenName = givenName;
    }

    @Override
    public Map<String, ?> asMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("familyName", familyName);
      map.put("givenName", givenName);
      return map;
    }

    @Override
    public Subst unify(Subst subst, LogicValue other) {
      subst = subst.unify(familyName, ((PersonName) other).familyName);
      if (subst == null) {
        return null;
      }
      return subst.unify(givenName, ((PersonName) other).givenName);
    }

    @Override
    public LogicValue replace(Replacer replacer) {
      return new PersonName(replacer.replace(familyName), replacer.replace(givenName));
    }

    @Override
    public String toString() {
      return String.format("PersonName{%s, %s}", familyName, givenName);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof PersonName)) {
        return false;
      }

      PersonName other = (PersonName) o;
      return ((familyName == null) ? (other.familyName == null) : familyName.equals(other.familyName))
          && ((givenName == null) ? (other.givenName == null) : givenName.equals(other.givenName));
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(new Object[] {familyName, givenName});
    }
  }

  @Test
  public void unifySubFields() {
    Var family = new Var();
    Var given = new Var();
    Var name = new Var();

    new LogicAsserter()
        .stream(
            conj(
                same(family, "Doe"),
                same(given, "John"),
                same(name, new PersonName(family, given))))
        .startSubst()
        .put(name, new PersonName("Doe", "John"))
        .addRequestedVar(name)
        .workUnits(1)
        .test();
  }

  @Test
  public void failedUnification() {
    Var family = new Var();
    Var given = new Var();
    Var name = new Var();

    new LogicAsserter()
        .stream(
            conj(
                same(family, "Doe"),
                same(new PersonName("Fooey", given), new PersonName(family, "Barson"))))
        .workUnits(0)
        .test();
  }

  @Test
  public void nonTrivialUnification() {
    Var family = new Var();
    Var given = new Var();
    Var name = new Var();

    new LogicAsserter()
        .stream(
            conj(
                Cons.O.order(
                    Cons.list(Arrays.asList(family, given)),
                    Cons.list(Arrays.asList(1, 2, 3, 4))),
                same(new PersonName(family, given), name)))
        .workUnits(6)
        .addRequestedVar(name)
        .startSubst()
        .put(name, new PersonName(1, 2))
        .startSubst()
        .put(name, new PersonName(1, 3))
        .startSubst()
        .put(name, new PersonName(1, 4))
        .startSubst()
        .put(name, new PersonName(2, 3))
        .startSubst()
        .put(name, new PersonName(2, 4))
        .startSubst()
        .put(name, new PersonName(3, 4))
        .test();
  }

  static final class Foo implements LogicValue {
    public Map<String, ?> asMap() {
      return Collections.emptyMap();
    }

    @Override
    public Subst unify(Subst subst, LogicValue other) {
      return subst;
    }

    @Override
    public LogicValue replace(Replacer replacer) {
      return this;
    }
  }

  static final class Bar implements LogicValue {
    public Map<String, ?> asMap() {
      return Collections.emptyMap();
    }

    @Override
    public Subst unify(Subst subst, LogicValue other) {
      return subst;
    }

    @Override
    public LogicValue replace(Replacer replacer) {
      return this;
    }
  }

  @Test
  public void differingClassesCannotUnion() {
    Var x = new Var();
    Var y = new Var();

    new LogicAsserter()
        .stream(conj(same(x, y), same(x, new Foo()), same(y, new Bar())))
        .workUnits(0)
        .test();
  }
}
