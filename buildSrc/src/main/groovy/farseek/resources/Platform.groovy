package farseek.resources

import groovy.transform.*

@ToString(includeNames = true, includeSuperProperties = true)
class Platform extends Resource {
    final URI mavenUri
    final String mavenGroup, mcPublishId

    Platform(Map<String, String> props, Map<String, Map<String, String>> badgeProps) {
        super(props, badgeProps)
        mavenUri    = props.mavenUri?.asUri()
        mavenGroup  = props.mavenGroup
        mcPublishId = props.mcPublishId ?: name
    }

    URI getGroupPath() { mavenUri? (mavenUri / mavenGroup?.replace(".", "/")): null }
}
