package gdx.liftoff.data.project

import com.badlogic.gdx.Files
import com.badlogic.gdx.utils.GdxRuntimeException
import com.kotcrab.vis.ui.util.OsUtils
import gdx.liftoff.data.files.CopiedFile
import gdx.liftoff.data.files.ProjectFile
import gdx.liftoff.data.files.PropertiesFile
import gdx.liftoff.data.files.SettingsFile
import gdx.liftoff.data.files.SourceDirectory
import gdx.liftoff.data.files.SourceFile
import gdx.liftoff.data.files.gradle.GradleFile
import gdx.liftoff.data.files.gradle.RootGradleFile
import gdx.liftoff.data.files.path
import gdx.liftoff.data.languages.Java
import gdx.liftoff.data.platforms.Android
import gdx.liftoff.data.platforms.Assets
import gdx.liftoff.data.platforms.Platform
import gdx.liftoff.data.templates.Template
import gdx.liftoff.views.AdvancedProjectData
import gdx.liftoff.views.BasicProjectData
import gdx.liftoff.views.ExtensionsData
import gdx.liftoff.views.LanguagesData
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Contains data about the generated project.
 */
class Project(
    val basic: BasicProjectData,
    val platforms: Map<String, Platform>,
    val advanced: AdvancedProjectData,
    val languages: LanguagesData,
    val extensions: ExtensionsData,
    val template: Template
) {
    private val gradleFiles: Map<String, GradleFile>
    val files = mutableListOf<ProjectFile>()
    val rootGradle: RootGradleFile
    val properties = mutableMapOf(
        "org.gradle.daemon" to "true",
        "org.gradle.jvmargs" to "-Xms512M -Xmx1G -XX:MaxPermSize=1G -XX:MaxMetaspaceSize=1G",
        "org.gradle.configureondemand" to "false"
    )
    val gwtInherits = mutableSetOf<String>()
    val androidPermissions = mutableSetOf<String>()

    val reflectedClasses = mutableSetOf<String>()
    val reflectedPackages = mutableSetOf<String>()

    // README.md:
    var readmeDescription = ""
    val gradleTaskDescriptions = mutableMapOf<String, String>()

    init {
        gradleFiles = mutableMapOf()
        rootGradle = RootGradleFile(this)
        platforms.forEach { gradleFiles[it.key] = it.value.createGradleFile(this) }
        addBasicGradleTasksDescriptions()
    }

    private fun addBasicGradleTasksDescriptions() {
        if (advanced.generateReadme) {
            arrayOf(
                "idea" to "generates IntelliJ project data.",
                "cleanIdea" to "removes IntelliJ project data.",
                "eclipse" to "generates Eclipse project data.",
                "cleanEclipse" to "removes Eclipse project data.",
                "clean" to "removes `build` folders, which store compiled classes and built archives.",
                "test" to "runs unit tests (if any).",
                "build" to "builds sources and archives of every project.",
                "--daemon" to "thanks to this flag, Gradle daemon will be used to run chosen tasks.",
                "--offline" to "when using this flag, cached dependency archives will be used.",
                "--continue" to "when using this flag, errors will not stop the tasks from running.",
                "--refresh-dependencies" to "this flag forces validation of all dependencies. Useful for snapshot versions."
            ).forEach {
                gradleTaskDescriptions[it.first] = it.second
            }
        }
    }

    fun hasPlatform(id: String): Boolean = platforms.containsKey(id)

    fun getGradleFile(id: String): GradleFile = gradleFiles[id]!!

    fun addGradleTaskDescription(task: String, description: String) {
        if (advanced.generateReadme) {
            gradleTaskDescriptions[task] = description
        }
    }

    fun generate() {
        addBasicFiles()
        addJvmLanguagesSupport()
        addExtensions()
        template.apply(this)
        addPlatforms()
        addSkinAssets()
        addReadmeFile()
        saveProperties()
        saveFiles()
    }

    private fun addBasicFiles() {
        // Adding global assets folder:
        files.add(SourceDirectory(Assets.ID, ""))
        // Adding .gitignore:
        files.add(CopiedFile(path = ".gitignore", original = path("generator", "gitignore")))
    }

    private fun addJvmLanguagesSupport() {
        Java().initiate(this) // Java is supported by default.
        languages.getSelectedLanguages().forEach {
            it.initiate(this)
            properties[it.id + "Version"] = languages.getVersion(it.id)
        }
        languages.appendSelectedLanguagesVersions(this)
    }

    private fun addExtensions() {
        extensions.getSelectedOfficialExtensions().forEach { it.initiate(this) }
        extensions.getSelectedThirdPartyExtensions().forEach { it.initiate(this) }
    }

    private fun addPlatforms() {
        platforms.values.forEach { it.initiate(this) }
        SettingsFile(platforms.values).save(basic.destination)
    }

    private fun saveFiles() {
        rootGradle.save(basic.destination)
        gradleFiles.values.forEach { it.save(basic.destination) }
        files.forEach { it.save(basic.destination) }
    }

    private fun saveProperties() {
        // Adding libGDX version property:
        properties["gdxVersion"] = advanced.gdxVersion
        PropertiesFile(properties).save(basic.destination)
    }

    private fun addSkinAssets() {
        if (advanced.generateSkin) {
            // Adding GUI assets directory:
            files.add(SourceDirectory(Assets.ID, "ui"))

            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "uiskin.atlas"),
                    original = path("generator", "assets", "ui", "uiskin.atlas")
                )
            )
            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "uiskin.json"),
                    original = path("generator", "assets", "ui", "uiskin.json")
                )
            )
            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "uiskin.png"),
                    original = path("generator", "assets", "ui", "uiskin.png")
                )
            )
            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "font.fnt"),
                    original = path("generator", "assets", "ui", "font.fnt")
                )
            )
            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "font-list.fnt"),
                    original = path("generator", "assets", "ui", "font-list.fnt")
                )
            )
            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "font-subtitle.fnt"),
                    original = path("generator", "assets", "ui", "font-subtitle.fnt")
                )
            )
            files.add(
                CopiedFile(
                    projectName = Assets.ID, path = path("ui", "font-window.fnt"),
                    original = path("generator", "assets", "ui", "font-window.fnt")
                )
            )

            // Android does not support classpath fonts loading through skins.
            // Explicitly copying Arial font if Android platform is included:
            if (hasPlatform(Android.ID)) {
                arrayOf("png", "fnt").forEach {
                    val path = path("com", "badlogic", "gdx", "utils", "arial-15.$it")
                    files.add(
                        CopiedFile(
                            projectName = Assets.ID, path = path, original = path,
                            fileType = Files.FileType.Classpath
                        )
                    )
                }
            }
        }
    }

    private fun addReadmeFile() {
        if (advanced.generateReadme) {
            files.add(
                SourceFile(
                    projectName = "", fileName = "README.md",
                    content = """# ${basic.name}

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff).

$readmeDescription

## Gradle

This project uses [Gradle](http://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

${gradleTaskDescriptions.map { "- `${it.key}`: ${it.value}" }.sorted().joinToString(separator = "\n")}

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project."""
                )
            )
        }
    }

    fun includeGradleWrapper(logger: ProjectLogger) {
        arrayOf(
            "gradlew", "gradlew.bat", path("gradle", "wrapper", "gradle-wrapper.jar"),
            path("gradle", "wrapper", "gradle-wrapper.properties")
        ).forEach {
            CopiedFile(path = it, original = path("generator", it)).save(basic.destination)
        }
        basic.destination.child("gradlew").file().setExecutable(true)
        basic.destination.child("gradlew.bat").file().setExecutable(true)
        logger.logNls("copyGradle")
        val gradleTasks = advanced.gradleTasks
        if (gradleTasks.isNotEmpty()) {
            logger.logNls("runningGradleTasks")
            val commands = determineGradleCommand() + advanced.gradleTasks
            logger.log(commands.joinToString(separator = " "))
            val process = ProcessBuilder(*commands).directory(basic.destination.file())
                .redirectErrorStream(true).start()
            val stream = BufferedReader(InputStreamReader(process.inputStream))
            var line = stream.readLine()
            while (line != null) {
                logger.log(line)
                line = stream.readLine()
            }
            process.waitFor()
            if (process.exitValue() != 0) {
                throw GdxRuntimeException("Gradle process ended with non-zero value.")
            }
        }
    }

    private fun determineGradleCommand(): Array<String> {
        return if (OsUtils.isWindows()) {
            arrayOf("cmd", "/c", "gradlew")
        } else {
            arrayOf("./gradlew")
        }
    }
}

interface ProjectLogger {
    fun log(message: String)
    fun logNls(bundleLine: String)
}
