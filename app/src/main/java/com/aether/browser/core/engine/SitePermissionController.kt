package com.aether.browser.core.engine

import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession

/**
 * The single gate for web-origin permissions. The first milestone deliberately denies requests;
 * a future permission UI can be added here without teaching tabs or Compose about Gecko callbacks.
 */
class SitePermissionController : GeckoSession.PermissionDelegate {
    override fun onAndroidPermissionsRequest(
        session: GeckoSession,
        permissions: Array<out String>?,
        callback: GeckoSession.PermissionDelegate.Callback,
    ) = callback.reject()

    override fun onContentPermissionRequest(
        session: GeckoSession,
        perm: GeckoSession.PermissionDelegate.ContentPermission,
    ): GeckoResult<Int> = GeckoResult.fromValue(
        GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY,
    )

    override fun onMediaPermissionRequest(
        session: GeckoSession,
        uri: String,
        video: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
        audio: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
        callback: GeckoSession.PermissionDelegate.MediaCallback,
    ) = callback.reject()
}
