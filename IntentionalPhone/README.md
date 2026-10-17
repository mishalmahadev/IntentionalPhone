# Intentional Phone (prototype 0.1.0)

A local-only Android prototype that lets you choose a purpose and displays a small, dismissible floating reminder over other apps.

## Features in this prototype
- Purpose choices: Call, Text, Social media, Shopping, Searching, Chess, Reading, Navigation, Work, Other.
- Skip without selecting anything.
- Floating reminder that does not take keyboard focus; close it with ✕.
- Reminder can be dragged to another position.
- Stop reminder from the app or dismiss the floating chip.
- No `INTERNET` permission and no third-party dependencies.

## Important limitation
This is the first working prototype, not the full unlock-triggered version yet. Open Intentional Phone and select a purpose to start the overlay. Android has restrictions on starting activities/services from the background, so reliable prompts on every unlock need a separate, carefully permissioned implementation and testing on the target device. This prototype does not monitor which apps you use or collect usage history.

## Build and install
1. Install Android Studio on your computer.
2. Open this folder (`IntentionalPhone`) as an existing project.
3. Allow Android Studio to sync Gradle and install the Android SDK platform 35 if prompted.
4. Connect your Pixel 9 with USB debugging enabled, or use an emulator.
5. Click **Run** to install the app.
6. In Android settings, grant **Display over other apps** when prompted. On Android 13+, allow notifications if you want to see the foreground-service notification.
7. Open the app, choose a purpose, and test the floating reminder. Tap ✕ to dismiss it.

## Privacy
The manifest deliberately omits `INTERNET`. No backend, analytics, account, or cloud storage is included. Android itself displays the system permission screens and foreground-service notification.

## Requirements
- Android Studio with Android SDK Platform 35
- JDK 17 or 21
- Minimum Android version: Android 8.0 (API 26)

## Build an APK using GitHub Actions (phone-only workflow)

The repository includes `.github/workflows/android.yml`. After the project files are committed to a GitHub repository on the `main` or `master` branch:
1. Open the repository in a browser and select **Actions**.
2. Choose **Build Android APK**. If prompted, enable workflows for the repository.
3. Tap **Run workflow** (or wait for the first push-triggered build).
4. Open the completed workflow run and download the `IntentionalPhone-debug-apk` artifact.
5. GitHub downloads the artifact as a ZIP. Extract it on your phone and tap `app-debug.apk` to install it. Android may ask you to allow installs from the browser or file manager you used.

This produces a debug APK signed with Android's debug key, suitable for personal testing, not Play Store release. Keep the repository private if you do not want to publish the source.
