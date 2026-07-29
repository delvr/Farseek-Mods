package farseek.resources

import farseek.utils.*
import groovy.transform.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import static farseek.resources.Dependency.Type.*

@ToString(includeNames = true, includeSuperProperties = true)
class LocalMod extends Artifact {
    final String license
    final URI issuesPage
    final List<String> authors
    final List<Dependency> dependencies
    final SourceSet main, test
    final File sourceRoot, outputRoot
    final boolean skipBuild

    LocalMod(String version, Map<String, String> props, Map<String, Map<String, String>> badgeProps,
             Map<String, Platform> platforms, Map<String, Dependency> modDependencies,
             SourceSetContainer sourceSets) {
        super(version, props + [mavenName: props.mavenName ?: props.name], badgeProps, platforms)
        license      = props.license
        issuesPage   = props.issuesPage.asUri()
        authors      = props.authors
        skipBuild    = props.skipBuild ?: false
        dependencies = (props.dependencies ?: []).collect { modDependencies[it] }
        def sourceSet = {
            name == "farseek"? it: sourceSets.create("$it.name$pascalCaseName") { scala }
        }
        main = sourceSet(sourceSets.main)
        test = sourceSet(sourceSets.test)
        sourceRoot = main.allSource.srcDirs.first().parentFile
        outputRoot = main.output.first()
    }

    String prefixed(String prefix) { prefix + pascalCaseName }
    String taskName(Task base) { prefixed(base.name) }

    File getReadmeFile()   { sourceRoot / "README.md" }
    File getResourcesDir() { sourceRoot / "resources" }

    List<Dependency> dependencies(Dependency.Type type) { dependencies.findAll { it.type == type } }

    /** <a href="https://docs.neoforged.net/docs/gettingstarted/modfiles#neoforgemodstoml">neoforge.mods.toml</a>*/
    // https://github.com/Kira-NT/mc-publish
    List<String> metadata(String minecraftVersion) {
        String dependencyHeader = "dependencies.$name", mcPublishHeader = "mc-publish",
               dependencyMcPublishHeader = "${dependencyHeader}.${mcPublishHeader}"
        def dependency = { String name, String versionRange, Dependency.Type type = required ->
            // https://docs.neoforged.net/docs/gettingstarted/modfiles#dependency-configurations
            [[dependencyHeader]: [modId: name, type: type.neoForgeName, versionRange: versionRange]]
        }
        def modDependency = { Dependency dep ->
            dependency(dep.name, dep.isLocal? dep.version: dep.versionRange, dep.type) +
            // https://github.com/Kira-NT/mc-publish?tab=readme-ov-file#dependencies
            dep.platformIds.ifNonEmpty { [(dependencyMcPublishHeader): it.mapKeys { it.mcPublishId }] }
        }
        Map deps = [:]
        dependencies.forEach { if(it.type != embedded) deps += modDependency(it) }
        Toml.lines([license: license, issueTrackerURL: issuesPage,
            ["mods"]: [
                modId: name, displayName: displayName, description: description, version: version,
                credits: authors, logoFile: "${name}.logo.png", displayURL: homepage
            ], ["mixins"]: [config: "${name}.mixins.json"],
            [mcPublishHeader]: platformIds.mapKeys { it.mcPublishId }
            ] + // https://github.com/Kira-NT/mc-publish?tab=readme-ov-file#game-versions
            [[dependencyHeader]: dependency("minecraft", minecraftVersion)] + deps)
    }

    @Override List<String> getMarkdownParagraphs() { super.markdownParagraphs + [readmeFile.text] }
}
