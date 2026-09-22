# Aether Browser

Aether is an adaptive Android browser foundation built with Kotlin, Jetpack Compose, Material 3,
AndroidX adaptive window APIs, and Mozilla GeckoView. Beta 2 provides real independent browser
tabs, a visual tab overview, dedicated settings, and compact/tablet/desktop interfaces driven by
the current app window.

## Current release

`1.0.0-beta.2` is an early testing release. It is suitable for sideloading and UI testing, but it
is not yet intended to replace a daily-use browser. The app compiles and targets Android 17
(API 37), supports Android's Material You dynamic color, and falls back to the Aether palette on
older versions.

## Architecture

- `core/engine`: Gecko runtime creation and centralized, deny-by-default site permissions.
- `core/tabs`: application-owned sessions, observable tab state, and pure tab-selection policy.
- `core/navigation`: address interpretation and replaceable search-provider abstraction.
- `core/settings`: persistent DataStore settings.
- `ui/common`: Gecko display host, address field, and native internal pages.
- `ui/compact`: one-handed bottom browser controls.
- `ui/expanded`: tablet-style tab strip and top navigation controls.
- `ui/desktop`: pointer-first browser chrome for freeform Android desktop windows.
- `ui/tabs`: responsive visual tab overview.
- `ui/settings`: full-page application settings.

`AetherApplication` owns the browser core for the life of the app process. A `GeckoView` only
attaches to the selected session and releases that display attachment when Compose changes layout;
it never closes the session. This is what lets tabs survive rotation, resizing, and UI-mode changes.

## Build

Use JDK 17 or newer and an Android SDK containing API 37:

```text
./gradlew testDebugUnitTest assembleDebug
```

No analytics, advertising, account, or application telemetry dependency is included. Gecko
telemetry is explicitly disabled. Website permission requests are denied centrally until a safe
prompt UI is implemented.
