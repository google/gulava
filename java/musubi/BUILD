#  Copyright (c) 2015 Dmitry Neverov and Google
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy
#  of this software and associated documentation files (the "Software"), to deal
#  in the Software without restriction, including without limitation the rights
#  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#  copies of the Software, and to permit persons to whom the Software is
#  furnished to do so, subject to the following conditions:
#
#  The above copyright notice and this permission notice shall be included in all
#  copies or substantial portions of the Software.
#
#  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#  SOFTWARE.

package(default_visibility = ["//visibility:public"])

java_binary(
    name = "Demo",
    srcs = ["Demo.java"],
    deps = [":musubi"],
    main_class = "musubi.Demo",
)

CLAUSES = ["AppendClauses.java", "OrderClauses.java", "ReverseClauses.java"]

java_library(
    name = "core",
    srcs = glob(["*.java"], exclude = CLAUSES + ["Demo.java"]),
    plugins = [
        "//java/musubi/processor:make_logic_value_plugin",
    ],
    deps = [
        "//java/musubi/annotation",
        "//third_party/pcollections",
    ],
    visibility = ["//visibility:private"],
)

# We build the clauses in a separate target for a sad reason. This is to make
# the annotation processor get non-error TypeMirror instances when seeing
# auto-generated logic value types in the signatures of decomposing clauses. For
# instance, in a clause that decomposes a Cons cell into Cons<?, ?>, if Cons is
# not properly resolved, the TypeMirror has a qualified name of "<any>" and
# there seems to be no way to get to the "Cons" string.
#
# So we build the CLAUSES in a separate target, which means it gets valid
# DeclaredType instances when it encounters Cons<FOO, BAR>.
#
# Note that Bazel does not appear to allow us to reorder the order in which
# annotation processors are run, which may also solve the problem.
java_library(
    name = "musubi",
    srcs = CLAUSES,
    plugins = [
        "//java/musubi/processor:make_goal_factory_plugin",
    ],
    exports = [":core"],
    deps = [
        ":core",
        "//java/musubi/annotation",
    ],
)
