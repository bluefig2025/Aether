package com.aether.browser

import android.app.Application
import com.aether.browser.core.engine.GeckoEngine
import com.aether.browser.core.settings.SettingsRepository
import com.aether.browser.core.tabs.TabManager

class AetherApplication : Application() {
    lateinit var tabManager: TabManager
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // GeckoView services run under this package in dedicated Android processes, and Android
        // creates our Application in each of them. Starting another GeckoRuntime from a Gecko
        // helper process recursively spawns helpers until Gecko terminates the app. The browser
        // core and its sessions belong exclusively to the main application process.
        if (getProcessName() != packageName) return

        settingsRepository = SettingsRepository(this)
        tabManager = TabManager(GeckoEngine(this))
    }
}
