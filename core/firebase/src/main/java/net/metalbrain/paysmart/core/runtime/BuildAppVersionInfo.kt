package net.metalbrain.paysmart.core.runtime

import net.metalbrain.paysmart.core.common.runtime.AppVersionInfo
import net.metalbrain.paysmart.core.firebase.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildAppVersionInfo @Inject constructor() : AppVersionInfo {
    override val versionName: String
        get() = BuildConfig.APP_VERSION_NAME

    override val versionCode: Int
        get() = BuildConfig.APP_VERSION_CODE
}
