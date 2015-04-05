package musubi;

import musubi.EmptyStream;
import musubi.SolveStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic {@link Goal} factories.
 */
public class Goals {
  public static Goal same(final Object u, final Object v) {
    return new Goal() {
      public Stream run(Subst state) {
        state = state.unify(u, v);
        if (state == null) {
          return EmptyStream.INSTANCE;
        }
        return new SolveStep(state, EmptyStream.INSTANCE);
      }
    };
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

  public static Goal disj(Goal g1, Goal g2, Goal... gs) {
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

  public static Goal conj(Goal g1, Goal g2, Goal... gs) {
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

  public static Goal ordero(final Object sub, final Object full) {
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

  private Goals() {}
}
