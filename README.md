# Dascon Machine Daily Log (Android)

Repo: https://github.com/imsubhamm/dasconapp

Thin Android client for site operators (Daiya). Saves into the same **Machinery → Log Book** as the web ERP (`/api/machinery/log-book`). No separate server deploy — uses existing ERP APIs.

## What it does

- Sign in with Dascon ERP email/password
- Pick project + date (today or back-date)
- Tap a machine → fill operator, shift, meters, HSD issue/consumption, work
- Saves online; if network fails, queues offline and syncs later

## Build / install

```bash
export DEVELOPER_DIR=/Library/Developer/CommandLineTools
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Phone needs **USB debugging** on (Developer options) and must show up in `adb devices`.

API base URL is the live ERP host (`https://dasconsouraverp.com/api/` in `BuildConfig.API_BASE_URL`) — same users DB as the website.

## Permissions needed in ERP

Same as web Log Book: machinery log create/view for the user’s role.

## Not in v1

- Edit existing server logs (add-only from phone)
- Photo attachments
- Fuel stock availability banner (web has it; phone focuses on daily entry)
