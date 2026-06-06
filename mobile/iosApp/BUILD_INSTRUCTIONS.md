# iOS Build Instructions

## Prerequisites

- Xcode 15.2 or later
- macOS 12.0 or later
- iOS 14.0 deployment target

## Building from Command Line

### Build Shared Framework

Before building the iOS app, generate the KMM shared framework:

```bash
cd mobile
./gradlew :shared:linkReleaseFrameworkIosArm64 --no-daemon
```

Or use the provided script:

```bash
cd mobile/iosApp
./build_shared_framework.sh
```

### Build the iOS App

```bash
cd mobile/iosApp
xcodebuild clean build \
  -scheme iosApp \
  -configuration Release \
  -sdk iphoneos \
  -derivedDataPath build
```

### Build Archive for Distribution

```bash
cd mobile/iosApp
xcodebuild clean archive \
  -scheme iosApp \
  -configuration Release \
  -archivePath build/iosApp.xcarchive \
  -sdk iphoneos \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  CODE_SIGNING_ALLOWED=NO
```

### Export IPA (Unsigned)

```bash
cd mobile/iosApp

# Create export options plist
cat > ExportOptions.plist << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>method</key>
  <string>development</string>
  <key>compileBitcode</key>
  <false/>
</dict>
</plist>
EOF

# Export archive to IPA
xcodebuild -exportArchive \
  -archivePath build/iosApp.xcarchive \
  -exportPath build \
  -exportOptionsPlist ExportOptions.plist
```

## Building from Xcode IDE

1. Open the project in Xcode:
   ```bash
   open mobile/iosApp/iosApp.xcodeproj
   ```

2. Build shared framework first (if not already built):
   - Run: `cd mobile && ./gradlew :shared:linkReleaseFrameworkIosArm64 --no-daemon`

3. Select the iosApp scheme in Xcode

4. Select your target device or simulator

5. Press Cmd+B to build

6. Press Cmd+R to run

## Project Structure

- `iosApp.xcodeproj/` - Xcode project file (contains build configuration)
- `iosApp/` - Swift source files
  - `CivilizationSimulatorApp.swift` - App entry point
  - `ContentView.swift` - Main UI view
  - `SimulationViewModel.swift` - View model for simulation logic
- `Assets.xcassets/` - App icons and other assets
- `Info.plist` - App configuration
- `build_shared_framework.sh` - Helper script to build KMM framework
- `.gitignore` - Git ignore patterns for build artifacts

## Framework Integration

The iOS app links to the shared KMM framework built at:
```
../shared/build/xcode-frameworks/shared.framework
```

The framework provides:
- `SimulationEngine` - Core simulation logic
- `RomeEnduresScenario` - Predefined civilization scenario
- `MonteCarloRunner` - Statistical analysis of multiple runs
- `SimulationResult` - Result data model

## Development

### Key Dependencies

- **Swift 5.9** or later
- **iOS 14.0+** minimum deployment target
- **KMM Shared Framework** (generated from mobile/shared)

### Code Style

- Follow Swift conventions per Apple's Swift API Guidelines
- Use MARK comments to organize code sections
- Keep view code declarative using SwiftUI

### Testing

Tests can be run from Xcode:
1. Product -> Test (Cmd+U)
2. Or from command line:
   ```bash
   xcodebuild test -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15'
   ```

## Troubleshooting

### Framework Not Found

If you get "framework not found shared", ensure the shared framework is built:
```bash
cd mobile
./gradlew :shared:linkReleaseFrameworkIosArm64 --no-daemon
```

### Xcode Project Not Found

The Xcode project is generated and committed to the repository at:
```
mobile/iosApp/iosApp.xcodeproj
```

If missing, regenerate with the generation script in the parent repository.

### Code Signing Issues

For development builds without code signing:
- Set `CODE_SIGN_IDENTITY=""` in xcodebuild commands
- Set `CODE_SIGNING_REQUIRED=NO`
- Set `CODE_SIGNING_ALLOWED=NO`

For distribution, proper code signing certificates are required.

## CI/CD Integration

The GitHub Actions workflow `build-ios.yml` automates iOS builds:
1. Checks out the repository
2. Sets up Java 17 for Gradle
3. Sets up Xcode 15.2
4. Builds the shared framework
5. Builds the iOS archive
6. Exports unsigned IPA for development

See `.github/workflows/build-ios.yml` for details.
