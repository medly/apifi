# apifi
Spec driven HTTP APIs

![Test](https://github.com/medly/apifi/workflows/Test/badge.svg)

## Include in gradle project
1\. Add jitpack repository
```
maven {
    url 'https://jitpack.io'
    content {
        includeGroup "com.github.medly"
    }
}
```
2\. Add dependency
```
configurations {
    apifi
}
dependencies {
    apifi 'com.github.medly:apifi:<version>'
}
```

3\. Add gradle task to generate
```
task generate(type: JavaExec) {
    classpath = configurations.apifi
    main = "apifi.AppKt"
    args "${rootProject.rootDir}/<path-to-yml>"
    args "${rootProject.rootDir}/<output-path>"
    args "<base-package-name>"
}
```