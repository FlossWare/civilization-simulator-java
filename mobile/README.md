# Civilization Simulator - Mobile Apps (KMM)

Kotlin Multiplatform Mobile apps for the Civilization Simulator.

## Overview

This project contains native Android and iOS applications built with Kotlin Multiplatform Mobile (KMM), sharing the core simulation engine across both platforms.

### Project Structure

```
civilization-simulator-kmm/
├── shared/              # Shared Kotlin code (simulation engine)
│   ├── commonMain/      # Platform-agnostic code
│   ├── androidMain/     # Android-specific code
│   └── iosMain/         # iOS-specific code
├── androidApp/          # Android app (Jetpack Compose)
├── iosApp/              # iOS app (SwiftUI)
└── .github/workflows/   # CI/CD for automated builds
```

## Features

- ✅ **Shared simulation engine** - Same code runs on both platforms
- ✅ **Native UIs** - Jetpack Compose (Android), SwiftUI (iOS)
- ✅ **Parallel execution** - Monte Carlo analysis with coroutines
- ✅ **Reproducible** - Deterministic results with seeded RNG
- ✅ **Offline** - No backend required, runs entirely on device

## Quick Start

### Android

**Option 1: Download from GitHub Releases**
```bash
# Download latest APK
wget https://github.com/FlossWare/civilization-simulator-kmm/releases/latest/download/androidApp-release.apk

# Install on device
adb install androidApp-release.apk
```

**Option 2: Build from source**
```bash
# Build APK
./gradlew :androidApp:assembleRelease

# Install
adb install androidApp/build/outputs/apk/release/androidApp-release.apk
```

**Requirements**:
- Android 8.0 (API 26) or higher
- ~20MB storage

### iOS

**Option 1: AltStore (Recommended for sideloading)**
```bash
# Download IPA from releases
wget https://github.com/FlossWare/civilization-simulator-kmm/releases/latest/download/iosApp.ipa

# Install via AltStore
# 1. Install AltStore on your iOS device
# 2. Open the IPA in AltStore
# 3. AltStore will sideload the app
```

**Option 2: Build with Xcode**
```bash
# Build Kotlin framework
./gradlew :shared:linkReleaseFrameworkIosArm64

# Open in Xcode
open iosApp/iosApp.xcodeproj

# Build and run from Xcode
```

**Requirements**:
- iOS 14.0 or higher
- ~30MB storage
- Development certificate (for sideloading)

## Development

### Prerequisites

- JDK 17+
- Android Studio (for Android development)
- Xcode 15+ (for iOS development, macOS only)
- Kotlin 2.0+

### Build Commands

```bash
# Build shared framework
./gradlew :shared:build

# Run tests
./gradlew :shared:allTests

# Build Android APK
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:assembleRelease

# Build iOS framework
./gradlew :shared:linkDebugFrameworkIosArm64
./gradlew :shared:linkReleaseFrameworkIosArm64
```

## Automated Builds

GitHub Actions automatically builds both apps on every push:

- **Android**: `build-android.yml` - Creates APK artifacts
- **iOS**: `build-ios.yml` - Creates IPA artifacts (unsigned)

### Releases

Create a release by pushing a tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will automatically:
1. Build Android APK
2. Build iOS IPA
3. Create GitHub release
4. Upload artifacts to release

## Architecture

### Shared Code (Kotlin Multiplatform)

The `shared` module contains:
- **Simulation engine** - Pure functional design
- **Data models** - Immutable state objects
- **Business logic** - Technology, economy, population modules
- **Threading** - Kotlin coroutines for Monte Carlo parallelism

### Android App

- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **Threading**: Coroutines with Dispatchers.Default
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34

### iOS App

- **UI**: SwiftUI
- **Architecture**: MVVM
- **Threading**: Swift concurrency (async/await)
- **Minimum iOS**: 14.0
- **Target iOS**: 17.0

## Performance

Based on testing:

- **Single simulation**: ~100-200ms (2000+ years)
- **Monte Carlo (50 runs)**: ~3-5 seconds on modern devices
- **Memory usage**: <100MB peak
- **Battery impact**: Low (computation-bound, no networking)

## Deployment

### Android

**Development**:
- Build and install via USB debugging
- Upload to Firebase App Distribution
- Sideload APK directly

**Production**:
- Sign APK with release keystore
- Upload to Google Play Console
- Submit for review

### iOS

**Development**:
- Build and run from Xcode
- TestFlight internal testing
- AltStore sideloading

**Production**:
- Sign IPA with distribution certificate
- Upload to App Store Connect
- Submit for review

## License

GPL-3.0 - See LICENSE file for details

## Links

- **Main Project**: https://github.com/FlossWare/civilization-simulator-java
- **Releases**: https://github.com/FlossWare/civilization-simulator-kmm/releases
- **Issues**: https://github.com/FlossWare/civilization-simulator-kmm/issues
- **Desktop Version**: https://github.com/FlossWare/civilization-simulator-java/releases

## Support

For bugs, feature requests, or questions:
1. Check existing issues
2. Create new issue with:
   - Device model and OS version
   - Steps to reproduce
   - Expected vs actual behavior
   - Logs if available

---

**Built with**: Kotlin 2.0, Jetpack Compose, SwiftUI, KMM  
**Status**: Beta  
**Version**: 1.0  
**Last Updated**: 2026-06-06
