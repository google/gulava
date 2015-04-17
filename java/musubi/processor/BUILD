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

# Implementation of annotation processor for automatically creating LogicValue
# implementations

java_library(
    name = "processor",
    exported_plugins = [
        ":make_goal_factory_plugin",
        ":make_logic_value_plugin",
    ],
    exports = [
        "//java/musubi",
        "//java/musubi/annotation",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "processor_lib",
    srcs = glob(["*.java"]),
    deps = ["//java/musubi/annotation"],
)

java_plugin(
    name = "make_logic_value_plugin",
    processor_class = "musubi.processor.MakeLogicValueProcessor",
    deps = [":processor_lib"],
    visibility = ["//java/musubi:__pkg__"],
)

# TODO: Delete and replace uses with MakePredicates processor once available.
java_plugin(
    name = "make_goal_factory_plugin",
    processor_class = "musubi.processor.MakeGoalFactoryProcessor",
    deps = [":processor_lib"],
    visibility = ["//java/musubi:__pkg__"],
)
