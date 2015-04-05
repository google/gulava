# Unit tests for annotation processors
package(default_testonly = 1)

java_library(
    name = "package_info",
    srcs = ["package-info.java"],
    deps = ["//java/musubi/processor"],
)

java_test(
    name = "MakeLogicValueProcessorBuilderTest",
    srcs = ["MakeLogicValueProcessorBuilderTest.java"],
    deps = [
        ":package_info",
        "//java/musubi/processor",
        "//third_party/junit4",
    ],
)
