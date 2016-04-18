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
package gulava.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * Class for collecting clause method metadata and associating clauses with their corresonding
 * predicate. This is essentially a builder for a {@link List} of {@link Predicate}s.
 */
public final class ClauseMethods {
  private final Map<String, List<ExecutableElement>> clausesByPredicate;
  private final List<ExecutableElement> predicates;
  private final Messager messager;

  private ClauseMethods(Map<String, List<ExecutableElement>> clausesByPredicate,
      List<ExecutableElement> predicates, Messager messager) {
    this.clausesByPredicate = clausesByPredicate;
    this.predicates = Collections.unmodifiableList(new ArrayList<>(predicates));
    this.messager = messager;
  }

  /**
   * Prints errors for any invalid predicate methods found, and returns a list with only the valid
   * ones.
   */
  private static List<ExecutableElement> validatePredicates(
      List<ExecutableElement> predicateMethods, Messager messager) {
    List<ExecutableElement> validated = new ArrayList<>();
    for (ExecutableElement predicateMethod : predicateMethods) {
      int errors = 0;

      for (VariableElement parameter : predicateMethod.getParameters()) {
        if (!parameter.asType().toString().equals("java.lang.Object")) {
          messager.printMessage(Diagnostic.Kind.ERROR,
              "All parameters to predicate methods must be of type Object.",
              parameter);
          errors++;
        }
      }

      if (errors == 0) {
        validated.add(predicateMethod);
      }
    }
    return validated;
  }

  /**
   * Returns a new instance for collecting clauses for the given predicates. The returned instance
   * initially has no clause methods and they can be added with
   * {@link addClause(ExecutableElement)}. This method will print errors for any invalid predicates
   * in {@code predicates} and ignore them in further processing.
   */
  public static ClauseMethods withPredicates(
      List<ExecutableElement> predicates, Messager messager) {
    Map<String, List<ExecutableElement>> clausesByPredicate = new HashMap<>();

    predicates = validatePredicates(predicates, messager);

    for (ExecutableElement predicate : predicates) {
      if (null != clausesByPredicate.put(nameArity(predicate), new ArrayList<>())) {
        throw new IllegalStateException(
            "Possibly a user error was detected too late. Predicates with duplicate name/arity"
            + " String: " + predicate.getSimpleName() + " - " + nameArity(predicate));
      }
    }

    return new ClauseMethods(clausesByPredicate, predicates, messager);
  }

  public void addClause(ExecutableElement clause) {
    String nameArity = nameArity(clause);
    List<ExecutableElement> priorClauses = clausesByPredicate.get(nameArity);
    if (priorClauses == null) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Clause method without predicate method. Expect an abstract method with name and arity"
          + " of: " + nameArity,
          clause);
      return;
    }

    priorClauses.add(clause);
  }

  /**
   * Returns the predicate metadata collected. This should be called after
   * {@link #addClause(ExecutableElement)} has been called for every clause method in the class.
   * This method reports an error for any predicate that has no clauses.
   */
  public List<Predicate> predicateMetadata() {
    List<Predicate> metadata = new ArrayList<>();
    for (ExecutableElement predicate : predicates) {
      List<ExecutableElement> clauses = clausesByPredicate.get(nameArity(predicate));
      if (clauses.isEmpty()) {
        messager.printMessage(Diagnostic.Kind.ERROR, "No clauses found for predicate.",
            predicate);
        continue;
      }

      metadata.add(new Predicate(predicate, clauses));
    }
    return metadata;
  }

  /**
   * Returns a String in the form {@code [NAME]/[PARAM_COUNT]}, which is the actual key used when
   * {@code method} is used like a key in {@link #clausesByPredicate}.
   */
  private static String nameArity(ExecutableElement method) {
    return String.format("%s/%d",
        method.getSimpleName().toString().split("_", 2)[0],
        method.getParameters().size());
  }
}
