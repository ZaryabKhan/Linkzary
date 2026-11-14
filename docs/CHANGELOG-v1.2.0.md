# Linkzary v1.2.0 — Update Plan Implementation (Nov 2026)

Built from AppChief's update order based on verified Play reviews (Jun 2025–Oct 2026).
Stack: Kotlin + Jetpack Compose, Room, Hilt, WorkManager, Coil, Billing Library 9.1.0.

## What shipped

### P0 — Data safety
1. **Google Drive backup/restore** — full backup of all links + collections to the
   user's own Drive (private app-data folder, DRIVE_APPDATA scope only — Linkzary
   cannot see any other Drive content). Sign-in via Google account picker.
   Restore skips links already on the device.
2. **Data-loss fixes**
   - (a) Checked the whole codebase: no uninstall/reinstall advice exists anywhere
     (code, strings, docs). Nothing to remove.
   - (b) In-app export: JSON + CSV already existed; **new: HTML bookmarks export**
     in the standard Netscape format every browser can import.
   - (c) "Clear all data" already has a confirmation dialog with a red warning +
     item list. Verified intact, untouched.
3. **Play compliance — CONFIRMED COMPLIANT**
   - targetSdk = 36 (meets the Aug 2026 requirement)
   - Billing Library 9.1.0 (>= 8.0.0)
   - No release blocker.

### P1 — Bug fixes
4. **Saved-link detection** — save paths used exact string URL match or (in the
   Add-dialog path) no check at all. New `UrlNormalizer` treats http/https, www.,
   trailing slash, fragments and common tracking params as the same page; all
   save paths now warn "Link already saved".
   Plus a **Merge duplicates** action in Settings that unions tags/notes/pins and
   deletes only the extra copies.
5. **Collection picker scroll bug** — the move-to-collection dialog's list had no
   height cap, so with many collections the bottom entries were unreachable.
   Fixed with `heightIn(max = 400.dp)`.

### P2 — Features
6. **Share sheet out** — new Share action on every link (options bottom sheet).
7. **Browser bookmarks HTML import** + **favicon display** (favicon chip next to
   the domain on every card; favicons were stored but never shown).
8. **Tag autocomplete** in the link editor (chips of existing tags), tags/notes
   already available at share-save; Add dialog unchanged (metadata fetched
   automatically).
9. **Custom thumbnail per link** — new optional "Custom image URL" field in the
   editor; user image wins over the auto preview. DB v8.
10. **Revisit reminders** — per-link "Remind me" (date + time picker), fired by
    WorkManager notification, tap opens the app, auto-clears after firing.
    POST_NOTIFICATIONS requested on Android 13+.

### Not built (per backlog rules)
Material 3 Expressive, tablet/foldable layouts, fingerprint lock, Chrome OS/WearOS.

## Build

```
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit tests (UrlNormalizer, BookmarkHtmlService)
```

Requires JDK 17 and Android SDK 36 (`local.properties` → `sdk.dir`).

## Version
- versionCode 16, versionName 1.2.0

## Known issues / limits
- Drive backup uses the deprecated-but-stable GoogleSignIn API; works on Android
  8.0–16. If Google hard-removes it, migration to Credential Manager needed.
- Reminders open the app, not the link's detail screen (no deep-link wiring).
- On-device testing not done in this environment (no emulator): compiled, unit
  tested, not manually exercised on a device.
- New strings exist in English only; other locales fall back until translated.
