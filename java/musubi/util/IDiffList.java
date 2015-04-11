/*
 *  Copyright (c) 2015 Dmitry Neverov and Google
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
package musubi.util;

import musubi.Var;
import musubi.annotation.MakeLogicValue;

/**
 * Represents a difference list. A difference list contains two plain lists: the head and the hole.
 * The head is the start of the list, and the hole is some suffix of the head which indicates that
 * the actual list stops here. Keeping the hole allows an algorithm to bind it in O(1) time, which
 * means appending to a difference list is much faster than it would be with a regular list. It also
 * means that no matter what the hole is bound to, the original list is still the same.
 */
@MakeLogicValue(name = "DiffList")
abstract class IDiffList<HEAD, HOLE> {
  public abstract HEAD head();
  public abstract HOLE hole();

  public static IDiffList<Var, Var> empty() {
    Var node = new Var();
    return new DiffList<>(node, node);
  }
}
