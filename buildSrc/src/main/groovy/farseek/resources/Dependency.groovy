package farseek.resources

import groovy.transform.*

@ToString(includeNames = true, includeSuperProperties = true)
class Dependency extends Artifact {
    static enum Type {
        required("REQUIRED"), compatible("OPTIONAL"), incompatible("INCOMPATIBLE"), embedded(null)
        final String neoForgeName
        Type(String neoForgeName) { this.neoForgeName = neoForgeName }
    }
    final Type type
    final String minVersion, maxVersion

    Dependency(Map<String, String> props, Map<String, Map<String, String>> badgeProps,
               Map<String, Platform> platforms) {
        super(props, badgeProps, platforms)
        type       = props.type ?: Type.required
        minVersion = props.minVersion
        maxVersion = props.maxVersion
    }

    final String getCoordinatesWithRange() { "$moduleId:$versionRange" }
    // https://maven.apache.org/pom.html#dependency-version-requirement-specification
    final String     getVersionRange() { "[$minVersion, $maxVersion]" }
    final boolean getHasVersionRange() {  minVersion && maxVersion && minVersion != maxVersion }
}
