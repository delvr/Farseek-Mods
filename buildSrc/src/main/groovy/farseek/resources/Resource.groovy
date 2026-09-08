package farseek.resources

import farseek.utils.Markdown
import groovy.transform.*
import org.gradle.api.*

@EqualsAndHashCode(includes = "name") @ToString(includeNames = true)
class Resource implements Named, Describable {

    /** https://maven.apache.org/guides/mini/guide-naming-conventions.html#artifact-identifier */
    public static final var nameRegex = ~/^[a-z0-9-]*$/

    final String name, displayName, description
    final URI homepage, badgeUri
    final Map<URI, URI> badges

    Resource(Map<String, String> props, Map<String, Map<String, String>> badgeProps) {
        name        = props.name.validated("resource name") { it =~ nameRegex }
        displayName = props.displayName ?: capitalizedName
        description = props.description
        homepage    = props.homepage?.asUri()
        badgeUri    = props.badgeUri?.asUri()
        def badgeLink = { s, id -> s.replace("PLACEHOLDER", id?:"").asUri() }
        badges = (props.badges?:[]).collectEntries { var (name, id) = it.splitOnFirst(":")
            [badgeLink(badgeProps[name].badgeUri, id), badgeLink(badgeProps[name].homepage, id)]
        }
    }

    final String getCapitalizedName() { name.splitAndTrim("-")*.capitalize().joinWords() }
    final String getPascalCaseName()  { capitalizedName.replace(" ", "") }

    List<String> getMarkdownParagraphs() { ["### $displayName", badgesMarkdown, description] }
    String getBadgesMarkdown() { badges.collect { img, dest -> Markdown.imageLink(img, dest) }.joinWords() }
}
