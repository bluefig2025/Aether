package com.aether.browser.core.engine

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession

class GeckoEngine(context: Context) {
    val runtime: GeckoRuntime = GeckoRuntime.create(
        context,
        GeckoRuntimeSettings.Builder()
            .remoteDebuggingEnabled(false)
            .build(),
    ).also { it.notifyTelemetryPrefChanged(false) }

    fun createSession(): GeckoSession = GeckoSession().also { session ->
        session.open(runtime)
    }
}
