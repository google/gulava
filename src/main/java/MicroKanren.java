public class MicroKanren {
  static class Cons<A,D> {
    protected final A a;
    protected final D d;
    public Cons(A a, D d) {
      this.a = a;
      this.d = d;
    }
    public A car() {return a;}
    public D cdr() {return d;}
    static <A,D> Cons<A,D> cons(A a, D d) {
      return new Cons<A,D>(a, d);
    }
    public String toString() {
      return "(" + a + " . " + d + ")";
    }
  }

  static class Var {
    private final int c;
    public Var(int c) {
      this.c = c;
    }
    public boolean equals(Object o) {
      return (o instanceof Var) && this.c == ((Var) o).c;
    }
    public String toString() {return "v" + c;}
  }
  static boolean isVar(Object o) {
    return o instanceof Var;
  }

  static class Assoc extends Cons<Var, Object> {
    public Assoc(Var var, Object o) {
      super(var, o);
    }
  }

  static class Subst extends Cons<Assoc, Subst> {
    public Subst(Assoc assoc, Subst subst) {
      super(assoc, subst);
    }
    public Assoc assq(Object o) {
      if (a == null)
        return null;
      if (a.car().equals(o))
        return a;
      return d != null ? d.assq(o) : null;
    }
  }

  static Subst ext(Subst s, Var x, Object v) {
    return new Subst(new Assoc(x, v), s);
  }

  static Object walk(Object u, Subst s) {
    if (s == null || !isVar(u))
      return u;
    Assoc pr = s.assq(u);
    if (pr == null)
      return u;
    return walk(pr.cdr(), s);
  }

  //Extends s in such a way that u made equals with v, returns the resulted substitution
  //Returns null if there is no way to unify u and v
  static Subst unify(Object u, Object v, Subst s) {
    u = walk(u, s);
    v = walk(v, s);
    if (isVar(u) && isVar(v) && u.equals(v)) return s;
    if (isVar(u)) return ext(s, (Var) u, v);
    if (isVar(v)) return ext(s, (Var) v, u);
    if (u instanceof Cons && v instanceof Cons) {
      s = unify(((Cons)u).car(), ((Cons)v).car(), s);
      if (s ==  null) return null;
      return unify(((Cons)u).cdr(), ((Cons)v).cdr(), s);
    }
    if (u.equals(v)) return s;
    return null;
  }

  static class State {
    final Subst s;
    final int c;
    public State(Subst s, int c) {
      this.s = s;
      this.c = c;
    }
    public String toString() {return "{" + (s == null ? "()" : s) +", " + c + "}";}
  }

  interface Stream {}
  static abstract class ImmatureStream implements Stream {
    abstract Stream realize();
    public String toString() {return "immature";}
  }
  static class MatureStream extends Cons<State, Stream> implements Stream {
    public MatureStream(State a, Stream d) {super(a, d);}
  }


  interface Goal {
    Stream run(State s);
  }

  interface GoalFn {
    Goal call(Var v);
  }

  static Goal same(final Object u, final Object v) {
    return new Goal() {
      public Stream run(State state) {
        Subst s = unify(u, v, state.s);
        if (s == null) return null;
        return unit(new State(s, state.c));
      }
    };
  }

  static Goal callFresh(final GoalFn f) {
    return new Goal() {
      public Stream run(State s) {
        return f.call(new Var(s.c)).run(new State(s.s, s.c + 1));
      }
    };
  }

  static Stream unit(State s) {
    return new MatureStream(s, null);
  }

  static Goal disj(final Goal g1, final Goal g2) {
    return new Goal() {
      public Stream run(State s) {
        return mplus(g1.run(s), g2.run(s));
      }
    };
  }

  static Goal conj(final Goal g1, final Goal g2) {
    return new Goal() {
      public Stream run(State s) {
        return bind(g1.run(s), g2);
      }
    };
  }

  static Stream mplus(final Stream s1, final Stream s2) {
    if (s1 == null)
      return s2;
    if (s1 instanceof ImmatureStream) {
      return new ImmatureStream() {
        Stream realize() {
          return mplus(s2, ((ImmatureStream) s1).realize());
        }
      };
    }
    MatureStream ms = (MatureStream) s1;
    return new MatureStream(ms.car(), mplus(ms.cdr(), s2));
  }

  static Stream bind(final Stream s, final Goal g) {
    if (s == null)
      return s;
    if (s instanceof ImmatureStream) {
      return new ImmatureStream() {
        Stream realize() {
          return bind(((ImmatureStream) s).realize(), g);
        }
      };
    }
    MatureStream ms = (MatureStream) s;
    return mplus(g.run(ms.car()), bind(ms.cdr(), g));
  }

  static class DelayedGoal implements Goal {
    private final GoalFn f;
    private final Var v;
    public DelayedGoal(GoalFn f, Var v) {
      this.f = f;
      this.v = v;
    }
    public Stream run(final State s) {
      return new ImmatureStream() {
        Stream realize() {
          return f.call(v).run(s);
        }
      };
    }
  }

  static Goal delayedCall(GoalFn f, Var v) {
    return new DelayedGoal(f, v);
  }

  static void print(Stream s) {
    print(s, Integer.MAX_VALUE);
  }

  static void print(Stream s, int n) {
    while (n > 0) {
      if (s == null) {
        System.out.println("()");
        return;
      }
      if (s instanceof ImmatureStream) {
        s = ((ImmatureStream) s).realize();
        continue;
      }
      MatureStream ms = (MatureStream) s;
      System.out.println(ms.car());
      n--;
      s = ms.cdr();
    }
  }

  static State emptyState() {
    return new State(null, 0);
  }

  public static void main(String... args) {
    final GoalFn fives = new GoalFn() {
      public Goal call(final Var v) {
        return disj(same(v, 5), delayedCall(this, v));
      }
    };
    final GoalFn sixes = new GoalFn() {
      public Goal call(final Var v) {
        Goal eq6 = same(v, 6);
        return disj(same(v, 6), delayedCall(this, v));
      }
    };
    Goal fivesAndSixes = callFresh(new GoalFn() {
      public Goal call(Var v) {
        return disj(fives.call(v), sixes.call(v));
      }
    });
    Stream s = fivesAndSixes.run(emptyState());
    print(s, 10);
  }
}
