# Release Process Guide

This document describes how to release a new version of the Civilization Simulator.

## Version Scheme

This project uses **X.Y versioning**:

- **X (major)**: Incompatible API changes, major architectural changes
- **Y (minor)**: New features, bug fixes, backwards-compatible changes

Examples: `1.0`, `1.1`, `2.0`, `2.1`

## Release Workflow

### Option 1: Automated via GitHub Actions (Recommended)

#### Step 1: Trigger Version Bump

1. Go to https://github.com/FlossWare/civilization-simulator-java/actions
2. Select "Version Bump" workflow
3. Click "Run workflow"
4. Choose bump type:
   - **major**: `1.5` → `2.0` (breaking changes)
   - **minor**: `1.5` → `1.6` (new features/fixes)
5. Click "Run workflow"

#### Step 2: Wait for Automatic Release

The workflow automatically:
- ✅ Updates version in `pom.xml`
- ✅ Updates version in `README.md`
- ✅ Updates version in `CivilizationSimulator.java`
- ✅ Commits changes
- ✅ Creates git tag (`v1.6`)
- ✅ Pushes tag
- ✅ **Triggers Release Workflow**

#### Step 3: Release Workflow Runs

The release workflow (triggered by tag push):
- ✅ Builds project with Maven
- ✅ Runs all tests
- ✅ Creates executable JAR with dependencies
- ✅ Generates checksums
- ✅ Creates GitHub Release with artifacts
- ✅ Deploys to PackageCloud.io
- ✅ Updates `latest` tag

### Option 2: Manual Release

If you prefer manual control:

```bash
# 1. Update version
mvn versions:set -DnewVersion=1.6 -DgenerateBackupPoms=false

# 2. Update README
sed -i 's/Version: .*/Version: 1.6/' README.md
sed -i 's/civilization-simulator-java-.*.jar/civilization-simulator-java-1.6.jar/g' README.md

# 3. Update Java source
sed -i 's/v[0-9.]*/v1.6/' src/main/java/org/flossware/civilization/CivilizationSimulator.java

# 4. Build and test
mvn clean package
java -jar target/civilization-simulator-java-1.6.jar single

# 5. Commit and tag
git add pom.xml README.md src/main/java/org/flossware/civilization/CivilizationSimulator.java
git commit -m "chore: bump version to 1.6"
git tag -a v1.6 -m "Release v1.6"

# 6. Push
git push && git push origin v1.6
```

The tag push triggers the automated release workflow.

## Monitoring Release Progress

### GitHub Actions

Watch workflows at: https://github.com/FlossWare/civilization-simulator-java/actions

- **Version Bump**: Updates version and creates tag (~30 seconds)
- **Release**: Builds, tests, and deploys (~2-3 minutes)

### Release Artifacts

After successful release, find artifacts at:

1. **GitHub Releases**: https://github.com/FlossWare/civilization-simulator-java/releases/tag/v1.6
   - `civilization-simulator-java-1.6.jar` (executable JAR)
   - `checksums.txt` (SHA256 checksums)
   - Auto-generated release notes

2. **PackageCloud**: https://packagecloud.io/FlossWare/civilization-simulator
   - JAR available for download

## Versioning Guidelines

### When to bump MAJOR (X)

- Breaking API changes
- Incompatible simulation changes (same seed → different results)
- Major architectural refactoring
- Removal of public APIs

**Example**: Changing module execution order, removing modules, changing state structure

### When to bump MINOR (Y)

- New features (new modules, new scenarios)
- Bug fixes
- Performance improvements
- Documentation updates
- Backwards-compatible API additions

**Example**: Adding a new module, fixing a calculation bug, adding new scenarios

## Pre-Release Checklist

Before releasing, ensure:

- [ ] All tests pass locally: `mvn test`
- [ ] Code builds without errors: `mvn clean package`
- [ ] JAR is executable: `java -jar target/*.jar single`
- [ ] Documentation is up-to-date
- [ ] CHANGELOG updated (if maintained)
- [ ] Breaking changes documented (for major versions)

## Post-Release Tasks

After successful release:

1. **Verify GitHub Release**
   - Check release notes are accurate
   - Download and test JAR artifact
   - Verify checksums

2. **Verify PackageCloud Deployment**
   - Visit packagecloud.io repository
   - Verify new version is listed
   - Test download link

3. **Update Documentation** (if needed)
   - Update examples with new version
   - Update installation instructions
   - Announce release (if public)

4. **Close Milestone** (if using GitHub milestones)
   - Review completed issues
   - Move incomplete issues to next milestone

## Rollback Procedure

If a release has critical bugs:

### Option 1: Quick Fix

1. Fix the bug
2. Bump minor version (e.g., `2.0` → `2.1`)
3. Release as normal

### Option 2: Delete Bad Release

```bash
# Delete tag locally
git tag -d v2.0

# Delete tag on remote
git push origin :refs/tags/v2.0

# Delete GitHub Release via web UI
# Then fix issue and re-release with same version
```

## CI/CD Pipeline Overview

```
┌─────────────────┐
│  Version Bump   │  (Manual trigger)
│   Workflow      │
└────────┬────────┘
         │
         ├─ Update pom.xml
         ├─ Update README.md
         ├─ Update Java source
         ├─ Git commit
         └─ Create & push tag (v1.6)
                 │
                 ▼
         ┌───────────────┐
         │    Release    │  (Auto-triggered by tag)
         │   Workflow    │
         └───────┬───────┘
                 │
                 ├─ Build & Test
                 ├─ Package JAR
                 ├─ Generate checksums
                 ├─ Create GitHub Release
                 ├─ Deploy to PackageCloud
                 └─ Update 'latest' tag
```

## Environment Variables

The release workflow uses:

- `GITHUB_TOKEN`: Automatically provided by GitHub Actions
- `PACKAGECLOUD_TOKEN`: Must be set as repository secret

## Troubleshooting

### Version bump workflow fails

**Symptom**: Workflow errors during git push
**Cause**: Insufficient permissions
**Fix**: Ensure `GITHUB_TOKEN` has `contents: write` permission (already configured)

### Release workflow fails at build step

**Symptom**: Maven build fails
**Cause**: Tests failing or compilation errors
**Fix**: Fix issues locally, commit fixes, re-run workflow

### PackageCloud deployment fails

**Symptom**: `package_cloud push` command fails
**Cause**: Missing or invalid `PACKAGECLOUD_TOKEN`
**Fix**: See [PACKAGECLOUD_SETUP.md](PACKAGECLOUD_SETUP.md)

### JAR is not executable

**Symptom**: `java -jar` fails with `no main manifest attribute`
**Cause**: Shade plugin misconfiguration
**Fix**: Verify `maven-shade-plugin` is configured correctly in `pom.xml`

## Getting Help

- GitHub Discussions: https://github.com/FlossWare/civilization-simulator-java/discussions
- Issues: https://github.com/FlossWare/civilization-simulator-java/issues
- Check workflow logs: https://github.com/FlossWare/civilization-simulator-java/actions

---

**Last Updated**: 2026-06-06
