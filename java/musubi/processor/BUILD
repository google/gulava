# Implementation of annotation processor for automatically creating LogicValue
# implementations

java_library(
    name = "processor",
    exported_plugins = [":make_logic_value_plugin"],
    exports = [
        "//java/musubi",
        "//java/musubi/annotation",
    ],
    visibility = ["//visibility:public"],
)

java_plugin(
    name = "make_logic_value_plugin",
    processor_class = "musubi.processor.MakeLogicValueProcessor",
    srcs = glob(["*.java"]),
)
