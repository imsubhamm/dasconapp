# Dascon Machine Daily Log (Android)

Thin Android client for site operators. Saves into the same **Machinery → Log Book** as the web ERP (`/api/machinery/log-book`).

## What it does

- Sign in with Dascon ERP email/password
- Pick project + date (today or back-date)
- Tap a machine → fill operator, shift, meters, HSD issue/consumption, work
- Saves online; if network fails, queues offline and syncs later

## Build / install

1. Open this folder in **Android Studio**
2. Wait for Gradle sync
3. Run on a phone/emulator (API 26+)

Or from CLI:

```bash
export DEVELOPER_DIR=/Library/Developer/CommandLineTools
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

API base URL is production Railway (`BuildConfig.API_BASE_URL`). Change in `app/build.gradle.kts` for staging.

## Permissions needed in ERP

Same as web Log Book: machinery log create/view for the user’s role.

## Not in v1

- Edit existing server logs (add-only from phone)
- Photo attachments
- Fuel stock availability banner (web has it; phone focuses on daily entry)
