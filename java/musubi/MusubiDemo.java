package musubi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Refactor this into test cases and proper documentation.
public class MusubiDemo {
  static Goal same(final Object u, final Object v) {
    return new Goal() {
      public Stream run(Subst state) {
        state = state.unify(u, v);
        if (state == null) {
          return EmptyStream.INSTANCE;
        }
        return unit(state);
      }
    };
  }

  static Stream unit(Subst s) {
    return new SolveStep(s, EmptyStream.INSTANCE);
  }

  private static List<Goal> goalList(Goal g1, Goal g2, Goal... gs) {
    List<Goal> allGoals = new ArrayList<>();
    allGoals.add(g1);
    allGoals.add(g2);
    for (Goal g : gs) {
      allGoals.add(g);
    }
    return allGoals;
  }

  static Goal disj(Goal g1, Goal g2, Goal... gs) {
    final List<Goal> allGoals = goalList(g1, g2, gs);
    return new Goal() {
      public Stream run(Subst s) {
        Stream result = allGoals.get(0).run(s);
        for (int i = 1; i < allGoals.size(); i++) {
          result = result.mplus(allGoals.get(i).run(s));
        }
        return result;
      }
    };
  }

  static Goal conj(Goal g1, Goal g2, Goal... gs) {
    final List<Goal> allGoals = goalList(g1, g2, gs);

    return new Goal() {
      public Stream run(Subst s) {
        Stream result = allGoals.get(0).run(s);
        for (int i = 1; i < allGoals.size(); i++) {
          result = result.bind(allGoals.get(i));
        }
        return result;
      }
    };
  }

  static class DelayedGoal implements Goal {
    private final Goal g;
    public DelayedGoal(Goal g) {
      this.g = g;
    }
    public Stream run(final Subst s) {
      return new ImmatureStream() {
        @Override
        protected Stream realize() {
          return g.run(s);
        }
      };
    }
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

  static Goal ordero(final Object sub, final Object full) {
    return new Goal() {
      @Override
      public Stream run(Subst s) {
        Var head = new Var();
        Var subTail = new Var();
        Var fullTail = new Var();

        return disj(
            conj(
                same(sub, null),
                same(full, null)),
            conj(
                same(new Cons(head, fullTail), full),
                disj(
                    conj(
                        same(new Cons(head, subTail), sub),
                        ordero(subTail, fullTail)),
                    ordero(sub, fullTail))))
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
  }
}
