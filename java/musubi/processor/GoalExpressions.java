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
package musubi.processor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Creates goal expressions, which can be separated into clause and predicate expressions. Several
 * clause expressions are disj'd together to form a predicate. In other words, each clause that a
 * predicate holds is an alternative way to satisfy the predicate.
 */
public final class GoalExpressions {
  private final String clauseInstance;
  private final Gensymer boundIds;
  private final Messager messager;

  /**
   * @param clauseInstance the object on which clause methods are invoked. This can correspond to a
   *     class name if the clause methods are static.
   * @param boundIds used to generate unique identifiers for "bound" logic values which are
   *     instantiated in preparation statements
   * @param messager where to print error messages
   */
  public GoalExpressions(String clauseInstance, Gensymer boundIds, Messager messager) {
    this.clauseInstance = clauseInstance;
    this.boundIds = boundIds;
    this.messager = messager;
  }

  public PreparedExpression predicate(Iterable<ExecutableElement> clauseMethods) {
    List<String> preparationStatements = new ArrayList<>();
    List<String> subGoalExpressions = new ArrayList<>();

    for (ExecutableElement clauseMethod : clauseMethods) {
      PreparedExpression clause = clause(clauseMethod);
      preparationStatements.addAll(clause.getPreparationStatements());
      subGoalExpressions.add(clause.getExpression());
    }

    return new PreparedExpression(
        Processors.compoundGoal("disj", subGoalExpressions), preparationStatements);
  }

  /**
   * Generates the expression for the invocation of a certain clause required by a predicate method
   * implementation. A predicate method needs at least one clause to be valid - each clause
   * represents an alternative way to reach the goal. This means each clause invocation should be
   * disj'd together to create the final {@code Goal}.
   *
   * <p>The {@code getExpression()} value of the returned instance is a conj of the goal expressions
   * which fulfill this clause. This includes the clause invocation itself as well as "same()"
   * (unification) goals which unify the Object reference given to the goal factory method with the
   * kind of reference required by the clause method.
   */
  private PreparedExpression clause(ExecutableElement clauseMethod) {
    // The actual arguments to pass to the clause method. If the clause method accepts
    // "Object" for an argument, this can just be the same value passed to the predicate
    // method.
    List<String> decomposedArgList = new ArrayList<>();

    List<String> subGoals = new ArrayList<>();
    List<String> preparationStatements = new ArrayList<>();

    for (VariableElement parameter : clauseMethod.getParameters()) {
      TypeMirror parameterType = parameter.asType();
      String parameterName = parameter.getSimpleName().toString();

      if ((parameterType instanceof DeclaredType)
          && Processors.qualifiedName((DeclaredType) parameterType)
              .contentEquals("java.lang.Object")) {
        // No need to decompose because the clause method accepts an Object reference.
        decomposedArgList.add(parameterName);
      } else {
        // We need to decompose this argument. Unify the Object reference passed to the goal
        // factory method to a reference of the desired type.
        String boundId = boundIds.gensym();
        decomposedArgList.add(boundId);
        String fresh = new FreshInstantiation(parameter, messager)
            .visit(parameterType);

        preparationStatements.add(
            String.format("    %s %s = %s;\n", parameterType, boundId, fresh));
        subGoals.add(String.format("%s.same(%s, %s)", ClassNames.GOALS, boundId, parameterName));
      }
    }

    // subGoals already has all the unification goals needed, if any. Now just add the
    // delegation to the clause method.
    subGoals.add(
        String.format("%s.%s(%s)",
            clauseInstance,
            clauseMethod.getSimpleName(),
            Processors.join(", ", decomposedArgList)));

    return new PreparedExpression(Processors.compoundGoal("conj", subGoals), preparationStatements);
  }
}
