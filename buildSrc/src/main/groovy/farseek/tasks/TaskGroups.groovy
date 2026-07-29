package farseek.tasks

abstract class TaskGroups {
    private static final String build = "build",
        classes = "classes", resources = "resources",
        buildSetup = "build setup", documentation = "documentation",
        publishing = "publishing", neoForgeInternal = "mod development/internal"
    static final Map<String, String> mappings = [
        "jar": build,
        "classes": classes, "compile": classes,
        "resources": resources, "metadata": resources,
        "readme": documentation, "publish": publishing,
        "preparekotlin": buildSetup, "publications": neoForgeInternal,
    ]
}
