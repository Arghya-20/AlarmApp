# Alarm App

Android alarm app (Kotlin + Jetpack Compose) with:
- Shake-to-stop
- Bedtime mode (auto screen-dim window, with a "pause for tonight" override)
- Material 3 theme, light/dark, plus 5 night color palettes (not just black)
- "Liquid glass" translucent card styling
- Calendar tab — set alarms on specific dates, view alarms/reminders per day
- Reminders + To-Do lists

## Get the APK (no local setup needed)

1. Create a new **public or private GitHub repo**.
2. Push this entire folder to it (see commands below).
3. GitHub Actions will automatically build a debug APK — check the **Actions** tab
   on your repo, open the latest run, and download the `alarm-app-debug-apk`
   artifact under "Artifacts" at the bottom of the run page.
4. Unzip it, transfer `app-debug.apk` to your phone, and install it (you'll need to
   allow "install from unknown sources" for whichever app you use to open it).

```bash
cd AlarmApp
git init
git add .
git commit -m "Initial alarm app"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

The workflow at `.github/workflows/build.yml` runs on every push to `main` and
can also be triggered manually from the Actions tab ("Run workflow").

## Build locally instead (Android Studio)

1. Open this folder in Android Studio (Koala or newer).
2. Let it sync Gradle (it will generate the wrapper automatically).
3. Run on a device/emulator, or Build > Build Bundle(s)/APK(s) > Build APK(s).

## Notes / things to double check

- First launch will prompt for notification permission and (on Android 12+)
  "Allow exact alarms" — both are required for alarms to fire reliably.
- Default alarm sound uses the system's default alarm ringtone; per-alarm custom
  sound picking (`sound_uri`) is wired in the data model but the picker UI isn't
  built yet — easy to add if you want it next.
- Shake sensitivity threshold is in `ShakeDetector.kt` (`thresholdG`) if it feels
  too sensitive or not sensitive enough on your device.
- Bedtime mode currently drives a settings flag + UI; hooking it up to actually
  dim/black the screen system-wide (e.g. via an overlay Activity/Service that
  launches at the scheduled time) is the next piece to wire in — happy to add
  that next if you want the full auto-trigger behavior.
