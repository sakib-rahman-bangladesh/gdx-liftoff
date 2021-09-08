package com.github.czyzby.setup.prefs

import com.github.czyzby.autumn.mvc.stereotype.preference.Property

/**
 * Saves Android SDK path. Needless to say, this setting is unlikely to change once set.
 * @author MJ
 */
@Property("AndroidSdk")
class AndroidSdkPreference : AbstractStringPreference() {
    override fun getDefault(): String {
        return System.getenv("ANDROID_SDK_ROOT").orEmpty()
    }
}
