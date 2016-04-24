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
package gulava;

import static gulava.Goals.conj;
import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.testing.AssertingWriter;
import gulava.testing.LogicAsserter;
import gulava.testing.RecordsCallGoal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class SubstTest {
  @Test
  public void mplusMultipleSubst() throws Exception {
    Stream s = Subst.EMPTY.ext(1, 2).mplus(Subst.EMPTY.ext(3, 4));
    AssertingWriter writer = new AssertingWriter();
    new Dumper(/*indentation=*/0, writer).dump(s);
    writer.write("----\n");
    new Dumper(/*indentation=*/0, writer).dump(s.mplus(Subst.EMPTY.ext(5, 6)));
    writer.assertLines(
        "SolveStep",
        "  Subst",
        "    1=2",
        "  Subst",
        "    3=4",
        "----",
        "SolveStep",
        "  Subst",
        "    1=2",
        "  SolveStep",
        "    Subst",
        "      3=4",
        "    Subst",
        "      5=6");
  }
}
