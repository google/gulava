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
import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.testing.AssertingWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;

@RunWith(JUnit4.class)
public class DumperTest {
  private static final Var A = new Var();
  private static final Var B = new Var();
  private static final Var C = new Var();
  private static final Var D = new Var();
  private static final Var E = new Var();

  private AssertingWriter writer;

  @Before
  public void setup() {
    writer = new AssertingWriter();
  }

  private Dumper dumper(int indentation) {
    return new Dumper(indentation, writer);
  }

  @Test
  public void simpleToString() throws Exception {
    Object o = new Object() {
      @Override
      public String toString() {
        return "!toString!";
      }
    };

    dumper(0).dump(o);
    dumper(7).dump(o);
    writer.assertLines(
        "!toString!",
        "       !toString!");
  }

  private static final Goal GOAL =
      conj(
          same(A, "foo"),
          same(B, "bar"),
          disj(
              new DelayedGoal(same("baz", C)),
              new RepeatedGoal(same("rrr", D)),
              same("bot", E)));

  @Test
  public void goals() throws Exception {
    dumper(3).dump(GOAL);

    writer.assertLines(
        "   ConjGoal",
        "     {" + A + " == foo}",
        "     {" + B + " == bar}",
        "     DisjGoal",
        "       DelayedGoal",
        "         {baz == " + C + "}",
        "       RepeatedGoal",
        "         {rrr == " + D + "}",
        "       {bot == " + E + "}");
  }

  @Test
  public void streams() throws Exception {
    Stream stream = GOAL.run(Subst.EMPTY);
    dumper(0).dump(stream);
    writer.write("----------\n");
    dumper(0).dump(stream.solve().rest());

    writer.assertLines(
        "ImmatureStream(mplus)",
        "  EmptyStream",
        "  ImmatureStream(mplus)",
        "    SolveStep",
        "      Subst",
        "        " + A + "=foo",
        "        " + B + "=bar",
        "        " + E + "=bot",
        "      EmptyStream",
        "    ImmatureStream(mplus)",
        "      SolveStep",
        "        Subst",
        "          " + A + "=foo",
        "          " + B + "=bar",
        "          " + D + "=rrr",
        "        ImmatureStream(DelayedGoal)",
        "          RepeatedGoal",
        "            {rrr == " + D + "}",
        "          Subst",
        "            " + A + "=foo",
        "            " + B + "=bar",
        "      ImmatureStream(DelayedGoal)",
        "        {baz == " + C + "}",
        "        Subst",
        "          " + A + "=foo",
        "          " + B + "=bar",
        "----------",
        "SolveStep",
        "  Subst",
        "    " + A + "=foo",
        "    " + B + "=bar",
        "    " + E + "=bot",
        "  SolveStep",
        "    Subst",
        "      " + A + "=foo",
        "      " + B + "=bar",
        "      " + D + "=rrr",
        "    ImmatureStream(mplus)",
        "      SolveStep",
        "        Subst",
        "          " + A + "=foo",
        "          " + B + "=bar",
        "          " + C + "=baz",
        "        EmptyStream",
        "      ImmatureStream(DelayedGoal)",
        "        RepeatedGoal",
        "          {rrr == " + D + "}",
        "        Subst",
        "          " + A + "=foo",
        "          " + B + "=bar");
  }

  @Test
  public void canFlush() throws Exception {
    writer.assertFlushes(0);
    dumper(0).dump(GOAL);
    writer.assertFlushes(0);
    dumper(0).flush();
    writer.assertFlushes(1);
  }
}
