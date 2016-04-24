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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class ImmatureStream implements Dumpable, Stream {
  /**
   * Adds all the subcomponents which - when this stream is dumped - are dumped as direct children
   * of this stream.
   */
  protected abstract void addSubcomponents(Collection<Object> destination);

  @Override
  public final Subst subst() {
    return null;
  }

  @Override
  public final void dump(Dumper dumper) throws IOException {
    Collection<Object> subcomponents = new ArrayList<>();
    addSubcomponents(subcomponents);
    dumper.dump("ImmatureStream", subcomponents.toArray());
  }

  @Override
  public final Stream mplus(final Stream s2) {
    final ImmatureStream outer = this;

    return new ImmatureStream() {
      @Override
      public Stream rest() {
        return s2.mplus(outer.rest());
      }

      @Override
      public void addSubcomponents(Collection<Object> destination) {
        destination.add(s2);
        outer.addSubcomponents(destination);
      }
    };
  }

  @Override
  public final Stream bind(final Goal goal) {
    final ImmatureStream outer = this;

    return new ImmatureStream() {
      @Override
      public Stream rest() {
        return outer.rest().bind(goal);
      }

      @Override
      public void addSubcomponents(Collection<Object> destination) {
        destination.add(goal);
        outer.addSubcomponents(destination);
      }
    };
  }
}
