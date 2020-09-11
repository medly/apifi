# Apifi 

Spec driven REST APIs

![Build the Distribution](https://github.com/medly/apifi/workflows/Build%20the%20Distribution/badge.svg)

## Include in gradle project
1\. Add jitpack repository in settings.gradle:
```
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url 'https://jitpack.io'
            content {
                includeGroup 'com.medly.apifi'
            }
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == 'com.medly') {
                useModule("${requested.id}:${requested.id}.gradle.plugin:ab816d1a84")
            }
        }
    }
}
```
2\. Add plugin in build.gradle:
```
plugins {
    ....
    id "com.medly.apifi"
    ...
}
```

3\. Add gradle task to generate code:
```
apifi {
    openApiSpec file("path/to/spec-file")
    generatedSourceDir file("path/to/gen")
    basePackageName "com.your.package.name"
}
```

## Generate APIs

```
./gradlew apifi
```