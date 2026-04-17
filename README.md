# 📺 MiTV Player — Complete GitHub Build Guide

> Professional IPTV App | Kotlin + Jetpack Compose + ExoPlayer + Hilt

---

## 🗂️ Project Structure

```
MiTVPlayer/
├── .github/
│   └── workflows/
│       └── build.yml          ← GitHub Actions (APK auto-build)
├── app/
│   ├── build.gradle.kts       ← App dependencies
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/mitv/player/
│           ├── MainActivity.kt
│           ├── MiTVApplication.kt
│           ├── data/
│           │   ├── parser/     M3UParser.kt
│           │   ├── repository/ ChannelRepository, PreferencesRepository
│           │   └── service/    PlaybackService.kt
│           ├── di/             AppModule.kt (Hilt)
│           ├── domain/model/   Models.kt
│           └── ui/
│               ├── navigation/ NavGraph.kt
│               ├── screens/    Dashboard, Player, Settings, Onboarding
│               ├── components/ ChannelCard
│               ├── theme/      Theme.kt (Dark/Light/Gold), Typography.kt
│               └── viewmodel/  Main, Player, Theme ViewModels
├── gradle/
│   ├── libs.versions.toml     ← All dependency versions
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 GitHub Se APK Kaise Banayein — Step by Step

### STEP 1: GitHub Account Banao (agar nahi hai)
1. https://github.com par jao
2. **Sign Up** karo — free account kaafi hai

---

### STEP 2: Naya Repository Banao
1. GitHub dashboard par **"New"** button dabao (green button, top left)
2. Fill karo:
   - **Repository name:** `MiTVPlayer`
   - **Visibility:** Private (recommended) ya Public
   - **Add README:** ❌ mat karo (hamara apna hai)
3. **"Create repository"** dabao
4. Repository URL note karo: `https://github.com/YourUsername/MiTVPlayer`

---

### STEP 3: ZIP Upload Karo (Sabse Aasaan Tarika)
1. Apne naye repository mein jao
2. **"uploading an existing file"** link dabao
3. **ZIP extract karo** apne PC par
4. Saari files drag & drop karo GitHub par
   - IMPORTANT: Folder structure maintain karo
   - `.github/workflows/build.yml` file zaroor upload ho
5. **"Commit changes"** dabao

---

### STEP 4: GitHub Actions Check Karo
1. Repository mein **"Actions"** tab dabao (top menu)
2. Tumhara workflow **"Build MiTV Player APK"** show hoga
3. Agar green ✅ = Build successful!
4. Agar red ❌ = Error hai, click karke dekho kya error hai

---

### STEP 5: APK Download Karo
1. **Actions** tab → Latest successful run dabao
2. Neeche **"Artifacts"** section mein:
   - `MiTV-Player-Debug` → Download karo
3. ZIP extract karo → `app-debug.apk` milegi
4. Apne Android phone par install karo ✅

---

## ⚙️ Local Machine Par Build (Android Studio)

### Requirements:
- Android Studio Hedgehog (2023.1) ya newer
- JDK 17
- Android SDK API 35

### Steps:
```bash
# 1. Clone karo
git clone https://github.com/YourUsername/MiTVPlayer.git

# 2. Android Studio mein open karo
# File → Open → MiTVPlayer folder select karo

# 3. Gradle sync hoga automatically (~3-5 minutes first time)

# 4. Build karo
# Build → Build Bundle(s)/APK(s) → Build APK(s)

# 5. APK yahan milegi:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔧 App Configuration

### M3U Playlist Add Karo:
1. App open karo
2. Settings (⚙️) tap karo
3. **"Add M3U Playlist"** tap karo
4. Apna M3U URL dalo (IPTV provider se milega)
5. **"Add"** dabao → Channels load honge

### Example M3U URLs (testing ke liye):
```
# Free test playlist:
https://iptv-org.github.io/iptv/index.m3u

# Ya apne provider ka URL:
http://yourprovider.com:8080/get.php?username=USER&password=PASS&type=m3u_plus
```

---

## 🎨 Features

| Feature | Status |
|---------|--------|
| M3U URL Parsing | ✅ |
| Channel Categories | ✅ |
| Channel Search | ✅ |
| Favorites (Heart) | ✅ |
| Dark Theme | ✅ |
| Light Theme | ✅ |
| Premium Gold Theme | ✅ |
| ExoPlayer HLS/DASH | ✅ |
| Custom Player Controls | ✅ |
| Aspect Ratio Toggle | ✅ |
| Buffering Indicator | ✅ |
| Channel Offline Fallback | ✅ |
| MiTV Watermark | ✅ |
| Background Playback | ✅ |
| Settings Screen | ✅ |
| Multiple M3U Sources | ✅ |
| GitHub Actions CI/CD | ✅ |

---

## 🐛 Common Errors & Fixes

### Error: "Gradle sync failed"
```
Fix: File → Invalidate Caches → Restart
```

### Error: "SDK location not found"
```
Fix: local.properties file mein add karo:
sdk.dir=/Users/YourName/Library/Android/sdk   (Mac)
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk   (Windows)
```

### Error: "Cleartext HTTP traffic not permitted"
```
Fix: Already fixed — AndroidManifest mein usesCleartextTraffic=true hai
```

### GitHub Actions Error: "gradlew: Permission denied"
```
Fix: Locally run karo:
git update-index --chmod=+x gradlew
git commit -m "Fix gradlew permissions"
git push
```

---

## 📱 APK Install Karna

1. APK download karo phone par
2. Settings → Security → **Unknown Sources** enable karo
3. APK file tap karo → Install
4. Ya: `adb install app-debug.apk` (USB debugging se)

---

## 🔑 Release APK Sign Karna (Optional — Play Store ke liye)

```bash
# Keystore banao:
keytool -genkey -v -keystore mitv-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mitv

# build.gradle.kts mein signingConfigs add karo:
signingConfigs {
    create("release") {
        storeFile = file("mitv-release.jks")
        storePassword = "yourpassword"
        keyAlias = "mitv"
        keyPassword = "yourkeypassword"
    }
}
```

---

## 📞 Support

App kisi bhi issue ke liye GitHub Issues tab use karo ya WhatsApp karo.

**MiTV Player v1.0** | Made with ❤️ for Muslim Islam Network TV
