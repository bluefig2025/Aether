package com.aether.browser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aether.browser.core.navigation.LocalNetworkAccess
import com.aether.browser.ui.AetherApp

class MainActivity : ComponentActivity() {
    private lateinit var browserViewModel: BrowserViewModel
    private val localNetworkAccess = LocalNetworkAccess()
    private var pendingLocalAddress: String? = null
    private val requestLocalNetworkPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val address = pendingLocalAddress
        pendingLocalAddress = null
        if (granted && address != null && ::browserViewModel.isInitialized) {
            browserViewModel.navigate(address)
        } else if (!granted) {
            Toast.makeText(
                this,
                "Local network access is needed to open that address.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AetherApplication
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BrowserViewModel(app.tabManager, app.settingsRepository) as T
        }
        setContent {
            browserViewModel = viewModel(factory = factory)
            AetherApp(browserViewModel, onNavigate = ::navigateWithPlatformPermissions)
        }
    }

    private fun navigateWithPlatformPermissions(input: String) {
        val requiresPermission = Build.VERSION.SDK_INT >= 37 && localNetworkAccess.isRequired(input)
        val alreadyGranted = !requiresPermission || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_LOCAL_NETWORK,
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            browserViewModel.navigate(input)
        } else {
            pendingLocalAddress = input
            requestLocalNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (::browserViewModel.isInitialized && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            val handled = when {
                event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_L -> {
                    browserViewModel.focusAddressBar(); true
                }
                event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_T -> {
                    browserViewModel.newTab(); browserViewModel.focusAddressBar(); true
                }
                event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_W -> {
                    browserViewModel.browserState.value.selectedTabId?.let(browserViewModel::closeTab); true
                }
                event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_R -> {
                    browserViewModel.reload(); true
                }
                event.isAltPressed && event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                    browserViewModel.back(); true
                }
                event.isAltPressed && event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    browserViewModel.forward(); true
                }
                else -> false
            }
            if (handled) return true
        }
        return super.dispatchKeyEvent(event)
    }
}
