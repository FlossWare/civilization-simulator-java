# Mobile Apps - Android & iOS

The Civilization Simulator is now available as native mobile applications!

## 📱 Download

### Android
- **Repository**: https://github.com/FlossWare/civilization-simulator-kmm
- **Latest Release**: https://github.com/FlossWare/civilization-simulator-kmm/releases/latest
- **Direct APK**: [Download APK](https://github.com/FlossWare/civilization-simulator-kmm/releases/latest/download/androidApp-release.apk)

**Requirements**: Android 8.0 (API 26) or higher

### iOS
- **Repository**: https://github.com/FlossWare/civilization-simulator-kmm
- **Latest Release**: https://github.com/FlossWare/civilization-simulator-kmm/releases/latest
- **IPA (Sideload)**: Available in releases

**Requirements**: iOS 14.0 or higher

---

## ✨ Features

Both apps include:

- ✅ **Full simulation engine** - Same core as desktop version
- ✅ **Monte Carlo analysis** - Run 10-50 parallel simulations
- ✅ **Reproducible results** - Seeded RNG for consistent outcomes
- ✅ **Native UI** - Jetpack Compose (Android), SwiftUI (iOS)
- ✅ **Offline-first** - No internet connection required
- ✅ **Fast performance** - 50 simulations in 3-5 seconds

---

## 🚀 Installation

### Android Installation

**Method 1: Direct Install (Easiest)**
1. Download APK from releases
2. Enable "Install from Unknown Sources" in Settings
3. Open APK file to install
4. Grant necessary permissions

**Method 2: ADB Install**
```bash
adb install androidApp-release.apk
```

### iOS Installation

**Method 1: AltStore (Recommended)**
1. Install [AltStore](https://altstore.io/) on your iOS device
2. Download IPA from releases
3. Open IPA in AltStore
4. AltStore will sideload the app

**Method 2: Xcode (Developers)**
1. Clone the KMM repository
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Select your device
4. Build and run

---

## 🛠️ Technology

### Kotlin Multiplatform Mobile (KMM)

The mobile apps use **Kotlin Multiplatform Mobile** to share code:

```
┌─────────────────────────────────┐
│     Shared Kotlin Code          │
│  - Simulation engine            │
│  - Business logic               │
│  - Data models                  │
│  - Coroutines for parallelism   │
└────────┬──────────────┬─────────┘
         │              │
    ┌────▼────┐   ┌────▼────┐
    │ Android │   │   iOS   │
    │ Compose │   │ SwiftUI │
    └─────────┘   └─────────┘
```

**Benefits**:
- Same simulation results on all platforms
- Single codebase for business logic
- Native UIs for best user experience
- Independent platform optimizations

### Key Technologies

**Shared**:
- Kotlin 2.0
- Kotlin Coroutines (parallel execution)
- kotlinx.serialization (JSON)

**Android**:
- Jetpack Compose (UI)
- ViewModel (state management)
- Material Design 3

**iOS**:
- SwiftUI (UI)
- ObservableObject (state management)
- iOS native components

---

## 📊 Performance

Tested on mid-range devices:

| Operation | Android | iOS | Desktop |
|-----------|---------|-----|---------|
| Single Simulation (2000 years) | ~150ms | ~120ms | ~125ms |
| Monte Carlo (50 runs) | ~4s | ~3.5s | ~470ms |
| Memory Usage | <80MB | <90MB | <100MB |
| Battery Impact | Low | Low | N/A |

**Note**: Desktop is faster for Monte Carlo due to more CPU cores/threads

---

## 🔄 Automated Builds

The mobile apps are built automatically via GitHub Actions:

### On Every Push
- ✅ Build Android APK
- ✅ Build iOS IPA (unsigned)
- ✅ Run tests
- ✅ Upload artifacts (30-day retention)

### On Release Tag
- ✅ Create GitHub release
- ✅ Upload APK to release
- ✅ Upload IPA to release (for sideloading)
- ✅ Generate release notes

**Create a release**:
```bash
cd civilization-simulator-kmm
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions automatically builds and publishes the release!

---

## 🎯 Comparison

| Feature | Desktop (JAR) | Web UI | Android | iOS |
|---------|---------------|--------|---------|-----|
| **Platform** | Windows/Mac/Linux | Browser | Android 8+ | iOS 14+ |
| **Installation** | Java required | None | APK install | Sideload/App Store |
| **Performance** | Fastest | Fast | Fast | Fast |
| **Offline** | ✅ | ✅ | ✅ | ✅ |
| **Monte Carlo** | 50 runs ~470ms | 50 runs ~2-3s | 50 runs ~4s | 50 runs ~3.5s |
| **UI Type** | CLI | Web (responsive) | Native | Native |
| **Distribution** | GitHub Releases | Self-hosted | APK/Play Store | Sideload/App Store |

---

## 📖 Documentation

- **Mobile README**: https://github.com/FlossWare/civilization-simulator-kmm/blob/main/README.md
- **Main Project**: https://github.com/FlossWare/civilization-simulator-java
- **Deployment Guide**: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

---

## 🤝 Contributing

Want to improve the mobile apps?

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on both platforms
5. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## 📜 License

GPL-3.0 - Same as the main project

---

## 🔗 Links

- **Main Repository**: https://github.com/FlossWare/civilization-simulator-java
- **Mobile Repository**: https://github.com/FlossWare/civilization-simulator-kmm
- **Desktop Releases**: https://github.com/FlossWare/civilization-simulator-java/releases
- **Mobile Releases**: https://github.com/FlossWare/civilization-simulator-kmm/releases

---

**Status**: Beta  
**Last Updated**: 2026-06-06  
**Platforms**: Android 8.0+, iOS 14.0+
