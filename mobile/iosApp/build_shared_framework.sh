#!/bin/bash
set -e

# Build KMM shared framework for iOS
cd "$(dirname "$0")/.."

echo "Building shared KMM framework for iOS..."
./gradlew :shared:linkReleaseFrameworkIosArm64 --no-daemon

echo "Shared framework built successfully"
