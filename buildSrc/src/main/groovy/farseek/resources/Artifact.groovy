package farseek.resources

import groovy.transform.*

@ToString(includeNames = true, includeSuperProperties = true)
class Artifact extends Resource {
    final static String LocalVersion = "0+LOCAL"

    final String mavenGroup, mavenName, version, devClassifier
    final Map<Platform, String> platformIds

    Artifact(Map<String, String> props, Map<String, Map<String, String>> badgeProps,
             Map<String, Platform> platforms) {
        super(props + [badges: (props.badges?:[]) +
            (props.platformIds?:[:]).collect { k, v -> "$k:$v" }], badgeProps)
        platformIds   =(props.platformIds ?: [:]).mapKeys { platforms[it] }
        mavenGroup    = props.mavenGroup  ?: defaultPlatform.mavenGroup
        mavenName     = props.mavenName   ?: idForDefaultPlatform
        version       = props.version     ?: LocalVersion
        devClassifier = props.devClassifier
    }

    final boolean getIsLocal() { version == LocalVersion }

    final Platform    getDefaultPlatform() { platformIds.keySet().findResult() }
    final String getIdForDefaultPlatform() { platformIds[defaultPlatform] }

    final String getModuleId() { "$mavenGroup:$mavenName" }
    final String getCoordinates() { "$moduleId:$version" }
    final String getDevCoordinates() { [coordinates, devClassifier].joinNonEmpties(":") }
}
