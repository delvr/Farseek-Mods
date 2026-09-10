package farseek.resources

import groovy.transform.*

@ToString(includeNames = true, includeSuperProperties = true)
class Artifact extends Resource {
    final static String LocalVersion = "0+LOCAL"

    final String mavenGroup, mavenName, version, classifier
    final Map<Platform, String> platformIds
    final Platform hostPlatform
    List<Dependency> dependencies = []

    Artifact(Map<String, String> props, Map<String, Map<String, String>> badgeProps,
             Map<String, Platform> platforms) {
        super(props + [badges: (props.badges?:[]) + (props.platformIds?:[])], badgeProps)
        platformIds  = (props.platformIds?:[]).asMapEntries().mapKeys { platforms[it] }
        hostPlatform = platforms[props.hostPlatform]
        mavenGroup   = hostPlatform?.mavenGroup ?: props.mavenGroup
        mavenName    = hostPlatform? platformIds[hostPlatform]: props.mavenName ?: name
        version      = props.version ?: LocalVersion
        classifier   = props.classifier
    }

    final boolean getIsLocal() { version == LocalVersion }

    final String getModuleId()    { [mavenGroup, mavenName].joinWithColons() }
    final String getCoordinates() { [moduleId, version, classifier].joinWithColons() }

    final List<Dependency> transitiveDependencies(Dependency.Type type) {
        dependencies.collectMany { dep ->
            dep.dependencies.findAll { it.type == type } + dep.transitiveDependencies(type)
        }
    }

    final List<Dependency> dependencies(Dependency.Type type) {
        dependencies.findAll { it.type == type }
    }
}
