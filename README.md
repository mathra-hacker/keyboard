# MATHRA KEYBOARD

An Android keyboard (IME) built with Kotlin, with **Sinhala** and **Singlish** typing, neon themes, emoji support, a settings dashboard, and an optional cloud AI writing assistant.

- Package: `com.mathra.keyboard`
- Min SDK 26 (Android 8.0), Target SDK 34
- Developer: Denuwan Akash

## Features
- Sinhala keyboard and Singlish (romanized) typing with suggestions
- Neon themes
- Emoji panel
- Dashboard and About screen
- Optional AI Assist (off unless you add your own API key; never used in password/PIN fields)

## Build
1. Open the project in Android Studio (Hedgehog or newer).
2. Let Gradle sync.
3. Run on a device/emulator, then enable **MATHRA KEYBOARD** in *Settings → System → Languages & input → On-screen keyboard*.

## Privacy
Text is sent to an AI API only when you tap the AI Assist button. Password and PIN fields are always excluded. Your API key is entered inside the app and is not stored in this repository.
