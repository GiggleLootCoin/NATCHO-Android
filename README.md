# NATCHO — Natcho Average AI

Standalone native Android app for LittleRedBigSmile.

Included:
- Native Android UI; no browser shell.
- AUTO / ONLINE / OFFLINE routing.
- Local OpenAI-compatible endpoint support, with PocketPal default.
- Configurable online OpenAI-compatible endpoint and model.
- Android Text-to-Speech response with explicit failure status.
- Hands-free LIVE voice loop when Android speech recognition is available.
- In-app web search through DuckDuckGo HTML.
- Image-generation handoff through Pollinations.
- Camera capture entry point.
- Persistent provider settings.
- No API keys hard-coded into the APK.

Build verification: the Android source is compiled by GitHub Actions before an APK is considered ready.

A large GGUF is not bundled in this APK. NATCHO connects to a local model already running on the phone, such as PocketPal, or to an online provider.
