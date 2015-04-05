package(default_visibility = ["//visibility:public"])

java_binary(
    name = "MusubiDemo",
    srcs = ["MusubiDemo.java"],
    deps = [":musubi"],
    main_class = "musubi.MusubiDemo",
)

java_library(
    name = "musubi",
    srcs = glob(["*.java"], exclude = ["MusubiDemo.java"]),
    deps = ["//third_party/pcollections"],
)
