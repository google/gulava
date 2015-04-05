package musubi;

import static musubi.Goals.conj;
import static musubi.Goals.disj;
import static musubi.Goals.ordero;
import static musubi.Goals.same;

import java.util.Arrays;

// TODO: Refactor this into test cases and proper documentation.
public class MusubiDemo {
  static Stream unit(Subst s) {
    return new SolveStep(s, EmptyStream.INSTANCE);
  }

  static void print(Stream s, int n, Var... requestedVars) {
    while (n-- >= 0) {
      SolveStep solve = s.solve();
      if (solve == null) {
        System.out.println("()");
        break;
      }
      if (solve.subst() != null) {
        System.out.println(new View.Builder()
            .setSubst(solve.subst())
            .addRequestedVar(requestedVars)
            .build());
      }
      s = solve.rest();
    }
  }

  static Goal repeat(final Goal repeated) {
    return new Goal() {
      private final Goal delayed = new DelayedGoal(this);

      @Override
      public Stream run(Subst s) {
        return disj(repeated, delayed).run(s);
      }
    };
  }

  static Goal appendo(final Object a, final Object b, final Object ab) {
    return new Goal() {
      @Override
      public Stream run(Subst s) {
        Var afirst = new Var();
        Var arest = new Var();
        Var abrest = new Var();

        return disj(
            conj(
                same(a, null),
                same(b, ab)),
            conj(
                same(new Cons(afirst, arest), a),
                same(new Cons(afirst, abrest), ab),
                appendo(arest, b, abrest)))
            .run(s);
      }
    };
  }

  static Goal reverseo(Object a, Object b) {
    return reverseo(a, b, null);
  }

  static Goal reverseo(final Object a, final Object b, final Object bTail) {
    return new Goal() {
      @Override
      public Stream run(Subst s) {
        Var aFirst = new Var();
        Var aRest = new Var();

        return disj(
            conj(
                same(a, null),
                same(b, bTail)),
            conj(
                same(a, new Cons(aFirst, aRest)),
                new DelayedGoal(reverseo(aRest, b, new Cons(aFirst, bTail)))))
            .run(s);
      }
    };
  }

  public static void main(String... args) {
    Var x = new Var();
    Var y = new Var();

    System.out.println("example 1");
    Stream s = disj(repeat(same(x, 5)), repeat(same(x, 6))).run(Subst.EMPTY);
    print(s, 10, x);

    System.out.println("\nexample 2");
    s = repeat(disj(same(x, 5), same(x, 6))).run(Subst.EMPTY);
    print(s, 10, x);

    System.out.println("\nexample 3");
    Goal g = conj(same(x, y), same(x, 5));
    print(g.run(Subst.EMPTY), 10, x, y);

    System.out.println("\nexample 4");
    g = appendo(x, y, Cons.list(Arrays.asList(1, 2, 3, 4)));
    print(g.run(Subst.EMPTY), 10, x, y);

    System.out.println("\nexample 5");
    Var a = new Var();
    Var b = new Var();
    g = conj(
        same(x, y),
        same(x, Cons.list(Arrays.asList(2, 3, 4))),
        same(a, new Cons(42, x)),
        same(b, new Cons(43, y)));
    print(g.run(Subst.EMPTY), 10, a, b);

    System.out.println("\nexample 6");
    g = reverseo(Cons.list(Arrays.asList(4, 5, 6)), x);
    print(g.run(Subst.EMPTY), 10, x);

    System.out.println("\nexample 7");
    g = ordero(x, Cons.list(Arrays.asList(1, 2, 3, 4, 5)));
    print(g.run(Subst.EMPTY), 50, x);

    System.out.println("\nexample 8");
    g = ordero(
        Cons.list(Arrays.asList(x, y)),
        Cons.list(Arrays.asList(1, 2, 3, 4, 5)));
    print(g.run(Subst.EMPTY), 50, x, y);

    System.out.println("\nexample 9");
    g = reverseo(x, y);
    print(g.run(Subst.EMPTY), 10, x, y);

    System.out.println("\nexample 10");
    g = same(x, x);
    print(g.run(Subst.EMPTY), 10, x, x);

    System.out.println("\nexample 11");
    g = conj(
        same(x, 42),
        same(y, Cons.list(Arrays.asList(5, 7, 9))),
        same(a, new Cons(x, y)));
    print(g.run(Subst.EMPTY), 10, a);
  }
}
