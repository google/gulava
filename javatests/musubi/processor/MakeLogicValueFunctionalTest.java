package musubi.processor;

import static musubi.Goals.same;

import musubi.LogicValue;
import musubi.Var;
import musubi.testing.LogicAsserter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
}
