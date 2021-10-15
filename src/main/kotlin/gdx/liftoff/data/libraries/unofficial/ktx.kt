@file:Suppress("unused") // Extension classes accessed via reflection.

package gdx.liftoff.data.libraries.unofficial

import com.badlogic.gdx.Gdx
import gdx.liftoff.data.libraries.Library
import gdx.liftoff.data.libraries.Repository
import gdx.liftoff.data.libraries.camelCaseToKebabCase
import gdx.liftoff.data.libraries.official.Ashley
import gdx.liftoff.data.libraries.official.Box2D
import gdx.liftoff.data.libraries.official.Freetype
import gdx.liftoff.data.platforms.Core
import gdx.liftoff.data.project.Project
import gdx.liftoff.views.Extension
import gdx.liftoff.views.fetchVersionFromMavenCentral
import khttp.get

/**
 * Modular Kotlin utilities.
 * @author libKTX organization
 */
abstract class KtxExtension : Library {
    override val defaultVersion = "1.10.0-b4"
    override val official = false
    override val repository = Repository.KTX
    override val group = "io.github.libktx"
    override val name
        get() = id.camelCaseToKebabCase()
    override val url: String
        get() = "https://github.com/libktx/ktx/tree/master/${id.removeSuffix("ktx").camelCaseToKebabCase()}"

    override fun initiate(project: Project) {
        project.properties["ktxVersion"] = latestKtxVersion
        addDependency(project, Core.ID, "$group:$name")
        initiateDependencies(project)
    }

    open fun initiateDependencies(project: Project) {}

    override fun addDependency(project: Project, platform: String, dependency: String) {
        super.addDependency(project, platform, "$dependency:\$ktxVersion")
    }

    fun addExternalDependency(project: Project, platform: String, dependency: String) {
        super.addDependency(project, platform, dependency)
    }
}

val latestKtxVersion by lazy {
    // Fetching and caching KTX version from the repo:
    try {
        get("https://raw.githubusercontent.com/libktx/ktx/master/version.txt").content.decodeToString().trim()
    } catch (exception: Exception) {
        Gdx.app.error("gdx-liftoff", "Unable to fetch KTX version from the repository.", exception)
        fetchVersionFromMavenCentral(KtxActors())
    }
}

/**
 * Kotlin utilities for Scene2D actors API.
 */
@Extension
class KtxActors : KtxExtension() {
    override val id = "ktxActors"
}

/**
 * General application listener utilities for Kotlin applications.
 */
@Extension
class KtxApp : KtxExtension() {
    override val id = "ktxApp"
}

/**
 * Utilities for Ashley entity component system.
 */
@Extension
class KtxAshley : KtxExtension() {
    override val id = "ktxAshley"

    override fun initiateDependencies(project: Project) {
        Ashley().initiate(project)
    }
}

/**
 * Kotlin utilities for assets management.
 */
@Extension
class KtxAssets : KtxExtension() {
    override val id = "ktxAssets"
}

/**
 * Kotlin utilities for asynchronous assets management.
 */
@Extension
class KtxAssetsAsync : KtxExtension() {
    override val id = "ktxAssetsAsync"

    override fun initiateDependencies(project: Project) {
        KtxAssets().initiate(project)
        KtxAsync().initiate(project)
    }
}

/**
 * Kotlin coroutines context and utilities for asynchronous operations.
 */
@Extension
class KtxAsync : KtxExtension() {
    override val id = "ktxAsync"

    override fun initiateDependencies(project: Project) {
        KotlinxCoroutines().initiate(project)
    }
}

/**
 * Kotlin Box2D utilities and type-safe builders.
 */
@Extension
class KtxBox2D : KtxExtension() {
    override val id = "ktxBox2d"

    override fun initiateDependencies(project: Project) {
        Box2D().initiate(project)
    }
}

/**
 * Kotlin utilities for libGDX collections.
 */
@Extension
class KtxCollections : KtxExtension() {
    override val id = "ktxCollections"
}

/**
 * Kotlin utilities for loading TrueType fonts via Freetype.
 */
@Extension
class KtxFreetype : KtxExtension() {
    override val id = "ktxFreetype"

    override fun initiateDependencies(project: Project) {
        Freetype().initiate(project)
    }
}

/**
 * Kotlin utilities for asynchronous loading of TrueType fonts via Freetype.
 */
@Extension
class KtxFreetypeAsync : KtxExtension() {
    override val id = "ktxFreetypeAsync"

    override fun initiateDependencies(project: Project) {
        KtxFreetype().initiate(project)
        KtxAsync().initiate(project)
    }
}

/**
 * Kotlin utilities for handling graphics in libGDX applications.
 */
@Extension
class KtxGraphics : KtxExtension() {
    override val id = "ktxGraphics"
}

/**
 * Kotlin utilities for internationalization.
 */
@Extension
class KtxI18n : KtxExtension() {
    override val id = "ktxI18n"
}

/**
 * Kotlin dependency injection without reflection usage.
 */
@Extension
class KtxInject : KtxExtension() {
    override val id = "ktxInject"
}

/**
 * libGDX JSON serialization utilities for Kotlin applications.
 */
@Extension
class KtxJson : KtxExtension() {
    override val id = "ktxJson"
}

/**
 * Kotlin utilities for zero-overhead logging.
 */
@Extension
class KtxLog : KtxExtension() {
    override val id = "ktxLog"
}

/**
 * Kotlin utilities for math-related classes.
 */
@Extension
class KtxMath : KtxExtension() {
    override val id = "ktxMath"
}

/**
 * libGDX preferences utilities for applications developed with Kotlin.
 */
@Extension
class KtxPreferences : KtxExtension() {
    override val id = "ktxPreferences"
}

/**
 * libGDX reflection utilities for applications developed with Kotlin.
 */
@Extension
class KtxReflect : KtxExtension() {
    override val id = "ktxReflect"
}

/**
 * Kotlin type-safe builders for Scene2D GUI.
 */
@Extension
class KtxScene2D : KtxExtension() {
    override val id = "ktxScene2d"
}

/**
 * Kotlin type-safe builders for Scene2D widget styles.
 */
@Extension
class KtxStyle : KtxExtension() {
    override val id = "ktxStyle"
}

/**
 * Tiled utilities for libGDX applications written with Kotlin.
 */
@Extension
class KtxTiled : KtxExtension() {
    override val id = "ktxTiled"
}

/**
 * Kotlin type-safe builders for VisUI widgets.
 */
@Extension
class KtxVis : KtxExtension() {
    override val id = "ktxVis"

    override fun initiateDependencies(project: Project) {
        VisUI().initiate(project)
    }
}

/**
 * Kotlin type-safe builders for VisUI widget styles.
 */
@Extension
class KtxVisStyle : KtxExtension() {
    override val id = "ktxVisStyle"

    override fun initiateDependencies(project: Project) {
        KtxStyle().initiate(project)
        VisUI().initiate(project)
    }
}
