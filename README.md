#Gulava

*Relational programming library for Java.*

Gulava allows you to write relational predicates in Java. You can write
Prolog-style predicates and use them from normal Java code, seamlessly
integrated with the magic of annotation processors.

See the [GitHub wiki](https://github.com/google/gulava/wiki/How-to-write-a-logic-value-type-and-predicate) for some how-tos and to get started writing your own predicates and logic value types.

This repository requires [Bazel](https://github.com/bazelbuild/bazel) to build,
test, and run. After you have set up Bazel, you can run the demo:

```
bazel run //java/gulava:Demo
```

Note that Gulava is not an official Google product.

### Contributing

Gulava doesn't have a particular long-term goal in mind besides exploring
relational programming. Any contributions to that end are welcome! That could be
something like:

- New relational programming features, like constraint support.
- Interesting demos, like the one in `java/gulava/Demo.java` but better.
- Data structures or other utilities, like `java/gulava/util/Queue.java`.

### Related
MicroKanren paper: http://webyrd.net/scheme-2013/papers/HemannMuKanren2013.pdf
