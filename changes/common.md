Internal project-wide changes:
- Refactored Gradle project structure from mod-specific subprojects to more lightweight mod-specific source sets.
- Migrated Gradle library version properties to the new Gradle TOML version catalog.
- Introduced a new mod-specific configuration mechanic using Groovy property files.
- Added support for (eventually) publishing to GitHub, CurseForge and Modrinth.
- Migrated Scala `into` modifiers to new bracketed syntax.
- Refactored the Scala code for easier development/maintenance.
- Tweaked IDEA Gradle and code-style settings.
- Removed now-invalid comments from JSON files.
- Updated Gradle, NeoForged and Scala versions.
