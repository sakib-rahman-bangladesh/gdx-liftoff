package gdx.liftoff.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.utils.ObjectMap
import com.github.czyzby.autumn.annotation.Processor
import com.github.czyzby.autumn.context.Context
import com.github.czyzby.autumn.context.ContextDestroyer
import com.github.czyzby.autumn.context.ContextInitializer
import com.github.czyzby.autumn.processor.AbstractAnnotationProcessor
import com.github.czyzby.lml.annotation.LmlActor
import devcsrj.mvnrepository.MvnRepositoryApi
import gdx.liftoff.config.inject
import gdx.liftoff.data.libraries.Library
import gdx.liftoff.data.libraries.Repository
import gdx.liftoff.data.libraries.unofficial.latestKtxVersion
import khttp.get

/**
 * HTTP request timeout when fetching extension versions.
 */
const val REQUEST_TIMEOUT = 15.0

/**
 * Holds data about official and third-party extensions.
 */
@Processor
class ExtensionsData : AbstractAnnotationProcessor<Extension>() {
    val official = mutableListOf<Library>()
    val thirdParty = mutableListOf<Library>()
    private val versionCache = mutableMapOf<Pair<String, String>, String>()

    @LmlActor("\$officialExtensions") private val officialButtons: ObjectMap<String, Button> = inject()
    @LmlActor("\$thirdPartyExtensions") private val thirdPartyButtons: ObjectMap<String, Button> = inject()

    fun getVersion(library: Library): String {
        val identifier = library.group to library.name
        if (identifier in versionCache) return versionCache[identifier]!!
        val version = when (library.repository) {
            Repository.MAVEN_CENTRAL -> fetchVersionFromMavenCentral(library)
            Repository.JITPACK -> fetchVersionFromJitPack(library)
            Repository.OTHER -> library.defaultVersion
            Repository.KTX -> latestKtxVersion
        }
        versionCache[identifier] = version
        return version
    }

    fun getSelectedOfficialExtensions(): Array<Library> = official.filter { officialButtons.get(it.id).isChecked }.toTypedArray()
    fun getSelectedThirdPartyExtensions(): Array<Library> = thirdParty.filter { thirdPartyButtons.get(it.id).isChecked }.toTypedArray()
    fun hasExtensionSelected(id: String): Boolean = (officialButtons.containsKey(id) && officialButtons.get(id).isChecked) || (thirdPartyButtons.containsKey(id) && thirdPartyButtons.get(id).isChecked)

    // Automatic scanning of extensions:
    override fun getSupportedAnnotationType(): Class<Extension> = Extension::class.java
    override fun isSupportingTypes(): Boolean = true
    override fun processType(
        type: Class<*>,
        annotation: Extension,
        component: Any,
        context: Context,
        initializer: ContextInitializer,
        contextDestroyer: ContextDestroyer
    ) {
        if (annotation.official) {
            official.add(component as Library)
        } else {
            thirdParty.add(component as Library)
        }
    }
}

fun fetchVersionFromMavenCentral(library: Library): String {
    val versions = MvnRepositoryApi.create().getArtifactVersions(library.group, library.name)
    if (versions.isNotEmpty()) return versions.first()
    // mvnrepository.com is much faster than Maven Central search, but it does not report
    // beta versions and release candidates. If no version was found, the application fallbacks
    // to the slower Maven Central search:
    try {
        val response = get(
            "https://search.maven.org/solrsearch/select", timeout = REQUEST_TIMEOUT,
            params = mapOf(
                "q" to """g:"${library.group}"+AND+a:"${library.name}"""",
                "rows" to "1",
                "wt" to "json",
            )
        )
        val results = response.jsonObject.getJSONObject("response").getJSONArray("docs")
        if (results.length() > 0) {
            return results.getJSONObject(0).getString("latestVersion")
        }
    } catch (exception: Exception) {
        Gdx.app.error("gdx-liftoff", "Unable to perform a HTTP request to Maven Central.", exception)
    }
    return library.defaultVersion
}

fun fetchVersionFromJitPack(library: Library): String {
    try {
        val response = get(
            "https://jitpack.io/api/builds/${library.group}/${library.name}/latest",
            timeout = REQUEST_TIMEOUT
        )
        return response.jsonObject.getString("version")
    } catch (exception: Exception) {
        Gdx.app.error("gdx-liftoff", "Unable to perform a HTTP request to JitPack.", exception)
    }
    return library.defaultVersion
}

/**
 * Should annotate all third-party extensions.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Extension(val official: Boolean = false)
