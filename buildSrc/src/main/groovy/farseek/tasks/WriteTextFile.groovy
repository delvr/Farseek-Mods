package farseek.tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

/** Task for writing to a text file, creating it if needed and replacing its contents if any. */
abstract class WriteTextFile extends DefaultTask {
    @Input      abstract Property<String> getText()
    @OutputFile abstract RegularFileProperty getOutputFile()
    @TaskAction void write() { outputFile.get().asFile.text = text.get() }
}
