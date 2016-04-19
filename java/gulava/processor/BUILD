#  Copyright (c) 2015 The Gulava Authors
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

# Implementation of annotation processor for automatically creating LogicValue
# implementations

java_library(
    name = "processor",
    exported_plugins = [
        ":make_goal_factory_plugin",
        ":make_logic_value_plugin",
        ":make_predicates_plugin",
    ],
    exports = [
        "//java/gulava",
        "//java/gulava/annotation",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "processor_lib",
    srcs = glob(["*.java"]),
    deps = [
        "//java/gulava/annotation",
        "//third_party/pcollections",
    ],
)

java_plugin(
    name = "make_logic_value_plugin",
    processor_class = "gulava.processor.MakeLogicValueProcessor",
    deps = [":processor_lib"],
    visibility = ["//java/gulava:__pkg__"],
)

java_plugin(
    name = "make_predicates_plugin",
    processor_class = "gulava.processor.MakePredicatesProcessor",
    deps = [":processor_lib"],
    visibility = ["//java/gulava:__pkg__"],
)

# TODO: Delete and replace uses with MakePredicates processor once available.
java_plugin(
    name = "make_goal_factory_plugin",
    processor_class = "gulava.processor.MakeGoalFactoryProcessor",
    deps = [":processor_lib"],
    visibility = ["//java/gulava:__pkg__"],
)
