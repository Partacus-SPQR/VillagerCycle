plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2"  // Default active version

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "build"
    ofTask("build")
}
