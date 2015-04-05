package musubi.processor;

import static musubi.Goals.conj;
import static musubi.Goals.same;

import musubi.LogicValue;
import musubi.Var;
import musubi.testing.LogicAsserter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;

/**
 * Test that makes sure that generated {@link LogicValue} implementations perform correctly as logic
 * values.
 */
@RunWith(JUnit4.class)
public class MakeLogicValueFunctionalTest {
  @Test
  public void unifyFields() {
    Var fooVar = new Var();
    Var barVar = new Var();
    Var simpleValueVar = new Var();

    new LogicAsserter()
        .stream(same(new SimpleValue("Doe", "John"), new SimpleValue(fooVar, barVar)))
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
                same(new SimpleValue(fooVar, "bar"), simpleValueVar)))
        .startSubst()
        .put(simpleValueVar, new SimpleValue("foo", "bar"))
        .addRequestedVar(simpleValueVar)
        .workUnits(2)
        .test();
  }

  @Test
  public void equality() {
    Assert.assertNotEquals(new SimpleValue("", null), new SimpleValue(null, ""));
    Assert.assertNotEquals(new SimpleValue("x", null), new SimpleValue("x", 42));
    Assert.assertNotEquals(new SimpleValue("q", 42), new SimpleValue("x", 42));
    Assert.assertEquals(new SimpleValue("x", "y"), new SimpleValue("x", "y"));

    Set<SimpleValue> values = new HashSet<SimpleValue>();
    for (int i = 0; i < 2; i++) {
      values.add(new SimpleValue("", null));
      values.add(new SimpleValue(null, ""));
      values.add(new SimpleValue(null, null));
      values.add(new SimpleValue("x", "y"));
      values.add(new SimpleValue("y", "x"));
    }

    Assert.assertEquals(5, values.size());

    Set<Integer> hashCodes = new HashSet<Integer>();
    for (SimpleValue value : values) {
      hashCodes.add(value.hashCode());
    }

    Assert.assertTrue("Expect 3 or more unique hash codes: " + hashCodes, hashCodes.size() >= 3);
  }

  @Test
  public void testToString() {
    Assert.assertEquals("SimpleValue(x, y)", new SimpleValue("x", "y").toString());
    Assert.assertEquals("SimpleValue(x, null)", new SimpleValue("x", null).toString());
  }
}
