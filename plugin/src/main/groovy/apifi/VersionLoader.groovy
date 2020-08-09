package apifi

import groovy.transform.CompileStatic

@CompileStatic
class VersionLoader {
    static final String VERSION_PROPERTIES_PATH = "/version.properties"

    private static String load() {
        def stream = VersionLoader.getResourceAsStream(VERSION_PROPERTIES_PATH)
        if (stream == null) {
            throw new RuntimeException("""\
                Could not load version.properties.
                This could happen when plugin jar is deleted
                Try Stopping Gradle Daemon (gradle --stop)""".stripIndent())
        }
        final Properties versions = new Properties()
        versions.load(stream);

        return versions['project.version'] ?: 'unspecified'
    }
}
