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
package gulava.lisp;

import static gulava.Goals.UNIT;
import static gulava.Goals.conj;
import static gulava.Goals.disj;
import static gulava.Goals.same;

import gulava.Cons;
import gulava.DelayedGoal;
import gulava.Goal;
import gulava.Var;
import gulava.annotation.MakePredicates;

/**
 * Predicates that implement a relational interpreter for a very simple Lisp dialect.
 */
@MakePredicates
public abstract class Lisp {
  public static final Lisp O = new MakePredicates_Lisp();

  /**
   * Evaluates the given expression with the given environment to obtain {@code result}. The
   * following forms are supported:
   *
   * <ul>
   *   <li>{@code env} - the environment, which is the accumulation of the arguments passed to all
   *       lambdas in the lexical scope
   *   <li>{@code (lambda [exp])} - creates a closure. The closure can be used as a function that
   *       takes any number of arguments. When calling that function, the arguments are pushed onto
   *       the environment stack so the last argument is on the top of the stack, and the
   *       environment can be accessed from inside {@code [exp]} with the {@code env} identifier
   *   <li>{@code (quote [literal])} - quotes a form. This causes {@code [literal]} to not be
   *       evaluated.
   *   <li>{@code cons car cdr} - these functions do what you expect.
   *   <li>{@code ([closure] [arg1] ... [argn])} - invokes the closure. The closure may be accessed
   *       from the environment (e.g. with {@code (car (cdr (cdr env)))}) or may be a literal
   *       {@code lambda} form.
   *   <li>{@code (case [expr] [null-branch] [cons-pair-branch] [function-branch])} - evaluates
   *       {@code expr}, and then executes one of the branches based on the value of {@code expr}.
   * </ul>
   */
  public abstract Goal eval(Object exp, Object env, Object result);

  final Goal eval_lambda(Cons<?, Cons<?, Void>> exp, Object env, Object result) {
    Object lambdaExp = exp.cdr().car();

    return conj(
        same("lambda", exp.car()),
        same(result, Closure.of(lambdaExp, env)));
  }

  final Goal eval_quote(Cons<?, Cons<?, Void>> exp, Object env, Object result) {
    return conj(
        same("quote", exp.car()),
        same(result, exp.cdr().car()));
  }

  final Goal eval_builtin(Object exp, Object env, Object result) {
    return conj(
        disj(
            same(exp, null),
            same(exp, "cons"),
            same(exp, "car"),
            same(exp, "cdr")),
        same(exp, result));
  }

  final Goal eval_env(Object exp, Object env, Object result) {
    return conj(
        same(exp, "env"),
        same(env, result));
  }

  final Goal eval_invoke(Object exp, Object env, Object result) {
    Cons<?, ?> evald = Cons.of(new Var(), new Var());

    return conj(
        evalEach(exp, env, evald),
        invoke(evald.car(), evald.cdr(), result));
  }

  final Goal eval_case(
      Cons<?, // "case"
      Cons<?, // expression to evaluate to get the selector value
      Cons<?, // null branch
      Cons<?, // cons pair branch
      Cons<?, // function branch
      Void>>>>> exp,
      Object env,
      Object result) {
    Object selectorExp = exp.cdr().car();
    Object nullBranch = exp.cdr().cdr().car();
    Object consPairBranch = exp.cdr().cdr().cdr().car();
    Object functionBranch = exp.cdr().cdr().cdr().cdr().car();

    Object selector = new Var();

    return conj(
        same(exp.car(), "case"),
        new DelayedGoal(
            conj(
                eval(selectorExp, env, selector),
                disj(
                    conj(same(null, selector), eval(nullBranch, env, result)),
                    conj(same(Cons.of(new Var(), new Var()), selector), eval(consPairBranch, env, result)),
                    conj(function(selector), eval(functionBranch, env, result))))));
  }

  /**
   * Indicates that some value is invokeable as a function.
   */
  public abstract Goal function(Object value);

  final Goal function_builtin(Object value) {
    return disj(
        same("cons", value),
        same("car", value),
        same("cdr", value));
  }

  final Goal function_user(Closure<?, ?> value) {
    return UNIT;
  }

  /**
   * Similar to {@link #eval(Object, Object, Object)}, but evaluates a list of expressions and
   * yields a list of respective results.
   */
  public abstract Goal evalEach(Object exps, Object env, Object results);

  final Goal evalEach_finish(Void exps, Object env, Void results) {
    return UNIT;
  }

  final Goal evalEach_iterate(Cons<?, ?> exps, Object env, Cons<?, ?> results) {
    return conj(
        new DelayedGoal(eval(exps.car(), env, results.car())),
        evalEach(exps.cdr(), env, results.cdr()));
  }

  /**
   * Invokes a function given a list of already-evaluated arguments.
   */
  public abstract Goal invoke(Object function, Object args, Object result);

  final Goal invoke_closure(Closure<?, ?> function, Object args, Object result) {
    Var newEnv = new Var();
    return conj(
        pushEnv(Cons.of(function, args), function.env(), newEnv),
        new DelayedGoal(eval(function.exp(), newEnv, result)));
  }

  final Goal invoke_cons(Object function, Cons<?, Cons<?, Void>> args, Object result) {
    return conj(
        same("cons", function),
        same(result, Cons.of(args.car(), args.cdr().car())));
  }

  final Goal invoke_car(Object function, Cons<Cons<?, ?>, Void> args, Object result) {
    return conj(
        same("car", function),
        same(result, args.car().car()));
  }

  final Goal invoke_cdr(Object function, Cons<Cons<?, ?>, Void> args, Object result) {
    return conj(
        same("cdr", function),
        same(result, args.car().cdr()));
  }

  /**
   * Pushes the values onto the old environment {@code pushOnto} to get the new environment
   * {@code newEnv}. The last value in {@code vals} will be on the top of the {@code newEnv} stack.
   */
  public abstract Goal pushEnv(Object vals, Object pushOnto, Object newEnv);

  final Goal pushEnv_finish(Void vals, Object pushOnto, Object newEnv) {
    return same(pushOnto, newEnv);
  }

  final Goal pushEnv_push(Cons<?, ?> vals, Object pushOnto, Object newEnv) {
    Cons<?, ?> pushed = Cons.of(vals.car(), pushOnto);
    return pushEnv(vals.cdr(), pushed, newEnv);
  }
}
