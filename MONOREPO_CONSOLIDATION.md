# Monorepo Consolidation - Complete

**Date**: 2026-06-06  
**Status**: ✅ Complete

## What Changed

The Kotlin Multiplatform Mobile (KMM) code has been **consolidated into this repository** under the `mobile/` directory. The separate `civilization-simulator-kmm` repository has been **archived**.

## Repository Structure

```
civilization-simulator-java/          (Main repository)
├── src/                              (Java desktop JAR)
├── web-ui/                           (Static web interface)
├── mobile/                           (NEW - KMM apps)
│   ├── shared/                       (Shared Kotlin simulation code)
│   ├── androidApp/                   (Jetpack Compose UI)
│   └── iosApp/                       (SwiftUI wrapper)
├── .github/workflows/
│   ├── cd-ci.yml                     (Java JAR release)
│   ├── build-android.yml             (Android APK builds)
│   └── build-ios.yml                 (iOS IPA builds)
└── README.md                         (Updated with mobile section)
```

## Benefits

### ✅ Single Source of Truth
- All platforms in one repository
- Unified issue tracking
- Single release tagging
- One place for documentation

### ✅ Easier Synchronization
- Java changes ↔ Kotlin ports stay in sync
- Single commit updates all platforms
- No cross-repo dependency management

### ✅ Unified Releases
```bash
# Create a release for ALL platforms
git tag v1.2
git push origin v1.2

# GitHub Actions automatically builds:
# - Desktop JAR
# - Android APK
# - iOS IPA
# All in one release!
```

### ✅ Simplified Development
- Clone once, build everything
- Single contributor workflow
- Consistent license/docs across platforms

## Migration Details

### Files Moved
- **50 Kotlin files** from `civilization-simulator-kmm/` → `mobile/`
- **2 GitHub Actions workflows** → `.github/workflows/`
- **1 README** → `mobile/README.md`

### Files Updated
- `README.md` - Added mobile section
- `MOBILE_APPS.md` - Updated all links to single repository
- `.github/workflows/build-android.yml` - Updated paths for monorepo
- `.github/workflows/build-ios.yml` - Updated paths for monorepo

### Files Removed
- Duplicate `LICENSE` (uses root LICENSE)
- Duplicate documentation files

### Old Repository
- **civilization-simulator-kmm** has been **archived**
- GitHub redirects: `FlossWare/civilization-simulator-kmm` → archived notice
- All future development happens in **civilization-simulator-java**

## Build Commands

### Desktop JAR
```bash
mvn clean package
java -jar target/civilization-simulator-java-1.1.jar monte
```

### Web UI
```bash
cd web-ui
java SimpleServer.java
# Open http://localhost:8080
```

### Android APK
```bash
cd mobile
./gradlew :androidApp:assembleRelease
# Output: mobile/androidApp/build/outputs/apk/release/*.apk
```

### iOS IPA (macOS only)
```bash
cd mobile/iosApp
xcodebuild clean archive \
  -scheme iosApp \
  -configuration Release \
  -archivePath build/iosApp.xcarchive
```

## Automated Builds

GitHub Actions **automatically builds all platforms**:

### On Every Push to `main`
- ✅ Java JAR (CI tests)
- ✅ Android APK (artifact upload)
- ✅ iOS IPA (artifact upload)

### On Release Tags (`v1.2`, `v2.0`, etc.)
- ✅ Create GitHub release
- ✅ Upload JAR to release
- ✅ Upload APK to release
- ✅ Upload IPA to release
- ✅ Deploy JAR to PackageCloud.io

**One tag = all platforms released!**

## Distribution

### GitHub Releases
**https://github.com/FlossWare/civilization-simulator-java/releases**

Each release includes:
- `civilization-simulator-java-X.Y.jar` (Desktop)
- `androidApp-release.apk` (Android)
- `iosApp.ipa` (iOS, unsigned for sideloading)

### PackageCloud.io
**https://packagecloud.io/FlossWare/civilization-simulator**

Maven repository for JAR distribution.

## Documentation

- **Main README**: [README.md](README.md)
- **Mobile Apps Guide**: [MOBILE_APPS.md](MOBILE_APPS.md)
- **Mobile Development**: [mobile/README.md](mobile/README.md)
- **Deployment Guide**: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- **Contributing**: [CONTRIBUTING.md](CONTRIBUTING.md)

## Platform Feature Parity

| Feature | Desktop JAR | Web UI | Android | iOS |
|---------|-------------|--------|---------|-----|
| Single simulation | ✅ | ✅ | ✅ | ✅ |
| Monte Carlo (50 runs) | ✅ | ✅ | ✅ | ✅ |
| Reproducible (seeded) | ✅ | ✅ | ✅ | ✅ |
| Parallel execution | ✅ | ✅ | ✅ | ✅ |
| Offline-first | ✅ | ✅ | ✅ | ✅ |
| Performance | Fastest | Fast | Fast | Fast |

## Next Steps

### For Users
- Download from: https://github.com/FlossWare/civilization-simulator-java/releases
- Install instructions: [MOBILE_APPS.md](MOBILE_APPS.md)

### For Developers
- Clone: `git clone https://github.com/FlossWare/civilization-simulator-java.git`
- Build all platforms from single repository
- See [CONTRIBUTING.md](CONTRIBUTING.md)

## Summary

✅ **Monorepo consolidation complete**  
✅ **All platforms in one repository**  
✅ **Automated builds configured**  
✅ **Documentation updated**  
✅ **Old repository archived**

**One repository, four platforms, unified development!**
