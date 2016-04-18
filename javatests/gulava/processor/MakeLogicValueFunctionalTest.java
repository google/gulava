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
package gulava.processor;

import static gulava.Goals.conj;
import static gulava.Goals.same;

import gulava.LogicValue;
import gulava.Var;
import gulava.annotation.MakeLogicValue;
import gulava.testing.LogicAsserter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test that makes sure that generated {@link LogicValue} implementations perform correctly as logic
 * values.
 */
@RunWith(JUnit4.class)
public class MakeLogicValueFunctionalTest {
  private static final Var X = new Var();

  @Test
  public void unifyFields() {
    Var fooVar = new Var();
    Var barVar = new Var();
    Var simpleValueVar = new Var();

    new LogicAsserter()
        .stream(same(SimpleValueInterface.of("Doe", "John"), SimpleValueInterface.of(fooVar, barVar)))
        .startSubst()
        .put(fooVar, "Doe")
        .put(barVar, "John")
        .addRequestedVar(fooVar, barVar)
        .workUnits(2)
        .test();
  }

  @Test
  public void unifyBasedOnOtherUnification() {
    // This should exercise the auto-generated LogicValue#replace implementation
    Var fooVar = new Var();
    Var simpleValueVar = new Var();

    new LogicAsserter()
        .stream(
            conj(
                same(fooVar, "foo"),
                same(SimpleValueInterface.of(fooVar, "bar"), simpleValueVar)))
        .startSubst()
        .put(simpleValueVar, SimpleValueInterface.of("foo", "bar"))
        .addRequestedVar(simpleValueVar)
        .workUnits(2)
        .test();
  }

  @Test
  public void equality() {
    Assert.assertNotEquals(SimpleValueInterface.of("", null), SimpleValueInterface.of(null, ""));
    Assert.assertNotEquals(SimpleValueInterface.of("x", null), SimpleValueInterface.of("x", 42));
    Assert.assertNotEquals(SimpleValueInterface.of("q", 42), SimpleValueInterface.of("x", 42));
    Assert.assertEquals(SimpleValueInterface.of("x", "y"), SimpleValueInterface.of("x", "y"));

    Set<SimpleValueInterface> values = new HashSet<SimpleValueInterface>();
    for (int i = 0; i < 2; i++) {
      values.add(SimpleValueInterface.of("", null));
      values.add(SimpleValueInterface.of(null, ""));
      values.add(SimpleValueInterface.of(null, null));
      values.add(SimpleValueInterface.of("x", "y"));
      values.add(SimpleValueInterface.of("y", "x"));
    }

    Assert.assertEquals(5, values.size());

    Set<Integer> hashCodes = new HashSet<Integer>();
    for (SimpleValueInterface value : values) {
      hashCodes.add(value.hashCode());
    }

    Assert.assertTrue("Expect 3 or more unique hash codes: " + hashCodes, hashCodes.size() >= 3);
  }

  @Test
  public void testToString() {
    Assert.assertEquals("SimpleValueInterface(x, y)", SimpleValueInterface.of("x", "y").toString());
    Assert.assertEquals(
        "SimpleValueInterface(x, null)", SimpleValueInterface.of("x", null).toString());
  }

  @MakeLogicValue
  interface NestedTypeLogicValue<F1, F2> {
    F1 field1();
    F2 field2();
  }

  @Test
  public void nestedTypeLogicValue() {
    Object value = new MakeLogicValue_MakeLogicValueFunctionalTest_NestedTypeLogicValue<>('a', 'b');
    Assert.assertEquals(
        new MakeLogicValue_MakeLogicValueFunctionalTest_NestedTypeLogicValue<>('a', 'b'),
        value);
    Var a = new Var();
    Var b = new Var();

    new LogicAsserter()
        .stream(same(
            value, new MakeLogicValue_MakeLogicValueFunctionalTest_NestedTypeLogicValue<>(a, b)))
        .startSubst()
        .put(a, 'a')
        .put(b, 'b')
        .addRequestedVar(a, b)
        .workUnits(2)
        .test();
  }

  @Test
  public void implementsInterfaces() {
    Assert.assertTrue(
        new MakeLogicValue_MakeLogicValueFunctionalTest_NestedTypeLogicValue<>(null, null)
            instanceof LogicValue);
    Assert.assertTrue(
        new MakeLogicValue_MakeLogicValueFunctionalTest_NestedTypeLogicValue<>(null, null)
            instanceof NestedTypeLogicValue);
    Assert.assertTrue(SimpleValueInterface.of(null, null) instanceof LogicValue);
    Assert.assertTrue(SimpleValueInterface.of(null, null) instanceof SimpleValueInterface);
  }

  @Test
  public void staticMethodsAreNotFields() {
    HasStaticMethod x = HasStaticMethod.of(42);
    Assert.assertEquals(Collections.singletonMap("foo", 42), x.asMap());
  }

  @MakeLogicValue
  abstract static class HasNoFields2 {}

  @Test
  public void canUnifyNoFieldValues() {
    new LogicAsserter()
        .stream(
            conj(
                same(X, new MakeLogicValue_MakeLogicValueFunctionalTest_HasNoFields2()),
                same(X, new MakeLogicValue_MakeLogicValueFunctionalTest_HasNoFields2())))
        .workUnits(2)
        .addRequestedVar(X)
        .startSubst()
        .put(X, new MakeLogicValue_MakeLogicValueFunctionalTest_HasNoFields2())
        .test();
  }

  @Test
  public void doesNotUnifyDifferingNoFieldTypes() {
    new LogicAsserter()
        .stream(
            conj(
                same(X, new MakeLogicValue_HasNoFields()),
                same(X, new MakeLogicValue_MakeLogicValueFunctionalTest_HasNoFields2())))
        .workUnits(1)
        .test();
  }

  private static void checkTypeAndConstructorModifiers(Class<?> clazz) {
    Assert.assertEquals(0, clazz.getModifiers() & Modifier.PUBLIC);
    Assert.assertEquals(Modifier.FINAL, clazz.getModifiers() & Modifier.FINAL);

    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    Assert.assertEquals(1, constructors.length);
    Assert.assertEquals(0,
        constructors[0].getModifiers()
            & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED));
  }

  @Test
  public void generatedClassAndConstructorArePackageProtected() {
    checkTypeAndConstructorModifiers(MakeLogicValue_SimpleValueInterface.class);
    checkTypeAndConstructorModifiers(MakeLogicValue_HasStaticMethod.class);
    checkTypeAndConstructorModifiers(
        MakeLogicValue_MakeLogicValueFunctionalTest_HasNoFields2.class);
  }
}
