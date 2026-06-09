# Gradle Wrapper Setup

## Current Status
The Gradle wrapper scripts (`gradlew` and `gradlew.bat`) and wrapper properties are in place, but the `gradle-wrapper.jar` binary is missing.

## To Complete the Setup

### Option 1: Automatic Generation (Recommended)
If you have Gradle installed locally:

```bash
cd mobile
gradle wrapper --gradle-version 8.5
```

### Option 2: Manual Download
Download the gradle-wrapper.jar from the official Gradle distributions and place it in:
- Location: `gradle/wrapper/gradle-wrapper.jar`
- Version: 8.5
- Source: https://services.gradle.org/distributions/gradle-8.5-bin.zip

Extract `gradle-8.5/lib/plugins/gradle-wrapper-8.5.jar` to `gradle/wrapper/gradle-wrapper.jar`

### Option 3: CI/CD Download
The GitHub Actions workflows will automatically download the wrapper JAR on first run via the `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties`.

## Files Present
- ✅ `mobile/gradlew` - Unix/Linux/macOS wrapper script
- ✅ `mobile/gradlew.bat` - Windows wrapper script  
- ✅ `mobile/gradle/wrapper/gradle-wrapper.properties` - Wrapper configuration (Gradle 8.5)
- ❌ `mobile/gradle/wrapper/gradle-wrapper.jar` - Binary JAR (will be downloaded on first use)

## Gradle Version
Gradle 8.5 is configured. This matches the requirements in `build.gradle.kts` for:
- Android Gradle Plugin 8.2.2
- Kotlin 2.0.0
- Kotlin Multiplatform
