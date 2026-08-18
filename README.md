# Shubh Power Android Wrapper

Android app for the public Shubh Power EV UI.

## Included
- First-launch 3-step onboarding with Skip / Continue / Get Started
- Android WebView that loads the existing Shubh Power UI
- JavaScript, DOM storage, geolocation, mixed-content compatibility
- External links open in the phone browser
- Back button follows WebView history
- Internet + location permissions

## Build APK in Android Studio
1. Open this folder in Android Studio.
2. Allow Gradle sync / install requested SDK 35 components.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Debug APK is generated under `app/build/outputs/apk/debug/app-debug.apk`.

For a distributable release APK, use **Build > Generate Signed App Bundle or APK**, choose APK, create/select a keystore, and build release.

## Important
The app renders the public website at runtime. Therefore the phone needs internet access and the public URL must remain available. If you need a fully offline/native APK, the original website source/assets are required.
