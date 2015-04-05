package musubi.processor;

import musubi.annotation.MakeLogicValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MakeLogicValueProcessorBuilderTest {
  @Test
  public void useBuilderToCreateValue() {
    SimpleValue value = new SimpleValue.Builder()
        .setFoo(42)
        .setBar("hello")
        .build();

    Assert.assertEquals(42, value.foo());
    Assert.assertEquals("hello", value.bar());
  }

  @Test
  public void fieldsDefaultToNull() {
    SimpleValue value = new SimpleValue.Builder()
        .setFoo(9)
        .build();
    Assert.assertEquals(9, value.foo());
    Assert.assertEquals(null, value.bar());
  }
}
