# Automated Release System

This project uses a **dual-workflow** release system borrowed from the FlossWare collections-java project.

## Overview

The project has **two automated workflows** for releases:

### 1. CD-CI Workflow (Primary) 🔄
**Auto-triggered on every push to `main` branch**

This is the **primary release mechanism** - it runs automatically whenever code is pushed to main.

#### What it does:
1. ✅ Auto-increments **minor version** (1.0 → 1.1 → 1.2...)
2. ✅ Updates dependencies to latest versions
3. ✅ Builds and tests the project
4. ✅ Deploys to PackageCloud.io
5. ✅ Creates git tag with new version
6. ✅ Commits version bump back to main

#### File: `.github/workflows/cd-ci.yml`

#### When to use:
- **Automatic** - no manual trigger needed
- Runs on every merge to main
- Perfect for continuous deployment

#### Version bumping formula:
```bash
# Increments Y in X.Y versioning
mvn build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}
```

### 2. Release Workflow (Secondary) 🏷️
**Manually triggered by creating version tags**

This workflow is for **explicit versioned releases** with GitHub Release artifacts.

#### What it does:
1. ✅ Extracts version from tag (`v1.5` → `1.5`)
2. ✅ Updates POM to match tag version
3. ✅ Builds and tests
4. ✅ Creates **GitHub Release** with:
   - Executable JAR
   - SHA256 checksums
   - Auto-generated release notes
5. ✅ Deploys to PackageCloud.io
6. ✅ Updates `latest` tag

#### File: `.github/workflows/release.yml`

#### When to use:
- **Manual** - create a tag to trigger
- For major versions or milestone releases
- When you want a GitHub Release page

#### How to trigger:
```bash
git tag -a v2.0 -m "Release v2.0 - Major refactor"
git push origin v2.0
```

---

## Comparison

| Feature | CD-CI Workflow | Release Workflow |
|---------|----------------|------------------|
| **Trigger** | Auto (push to main) | Manual (git tag) |
| **Version** | Auto-increment minor | Explicit from tag |
| **GitHub Release** | ❌ No | ✅ Yes |
| **PackageCloud** | ✅ Yes | ✅ Yes |
| **Git Tag** | ✅ Auto-created | ✅ Uses your tag |
| **Version Commit** | ✅ Commits back | ❌ No |
| **Dependencies** | ✅ Auto-updates | ❌ No |
| **Use Case** | Continuous deployment | Milestone releases |

---

## Typical Workflow

### Day-to-Day Development (CD-CI)

```bash
# 1. Make changes
git checkout -b feature/new-module
# ... code changes ...

# 2. Commit and push
git commit -m "feat: add new module"
git push origin feature/new-module

# 3. Create PR and merge to main
# GitHub PR → Merge

# 4. CD-CI auto-runs:
#    - Version: 1.5 → 1.6
#    - Builds, tests, deploys
#    - Tags as v1.6
#    - Commits version bump
```

**Result**: New version automatically deployed to PackageCloud!

### Major Release (Release Workflow)

```bash
# 1. Decide on major version
# Current: 1.8
# Next: 2.0

# 2. Create and push tag
git tag -a v2.0 -m "Release v2.0 - Breaking API changes"
git push origin v2.0

# 3. Release workflow runs:
#    - Creates GitHub Release
#    - Deploys to PackageCloud
#    - Updates latest tag
```

**Result**: GitHub Release page created with downloadable artifacts!

---

## Version Control Strategy

### Minor Versions (Auto)
- Every push to main auto-increments: `1.0 → 1.1 → 1.2`
- Handled by CD-CI workflow
- No manual intervention needed

### Major Versions (Manual)
- Manually create tag: `v2.0`, `v3.0`
- Handled by Release workflow
- Resets minor version manually if needed

### Example Timeline:
```
1.0  (initial release via tag)
1.1  (CD-CI auto-bump)
1.2  (CD-CI auto-bump)
1.3  (CD-CI auto-bump)
2.0  (manual tag for breaking changes)
2.1  (CD-CI auto-bump)
2.2  (CD-CI auto-bump)
```

---

## PackageCloud Deployment

Both workflows deploy to PackageCloud using **Maven deploy**:

```bash
mvn -DskipTests deploy
```

### Configuration Required

Add `PACKAGECLOUD_TOKEN` to GitHub Secrets:
1. Go to: https://github.com/FlossWare/civilization-simulator-java/settings/secrets/actions
2. Click "New repository secret"
3. Name: `PACKAGECLOUD_TOKEN`
4. Value: Your token from https://packagecloud.io/api_token

### Maven Configuration

The workflows use `s4u/maven-settings-action` to configure Maven settings.xml:

```yaml
- name: Configure Maven settings for PackageCloud
  uses: s4u/maven-settings-action@v3.1.0
  with:
    githubServer: false
    servers: |
      [
        {
          "id": "packagecloud-flossware",
          "configuration": {
            "httpHeaders": {
              "property": {
                "name": "Authorization",
                "value": "Bearer ${{ secrets.PACKAGECLOUD_TOKEN }}"
              }
            }
          }
        }
      ]
```

The `id` matches the `<distributionManagement>` repository ID in `pom.xml`.

---

## Workflow Files

### `.github/workflows/cd-ci.yml` (Primary)
- **Trigger**: `on: push: branches: [main]`
- **Skip condition**: Ignores pushes from `version-bump@flossware.org`
- **Version bump**: Auto-increments minor version
- **Dependency updates**: Updates JUnit to latest
- **Deployment**: PackageCloud via Maven
- **Tagging**: Creates version tag and commits back

### `.github/workflows/release.yml` (Secondary)
- **Trigger**: `on: push: tags: ['v*.*']`
- **Version**: Extracted from tag name
- **Artifacts**: Builds JAR + checksums
- **GitHub Release**: Creates release page
- **Deployment**: PackageCloud via Maven
- **Latest tag**: Updates `latest` tag

### `.github/workflows/ci.yml` (Basic CI)
- **Trigger**: `on: push/pull_request to main/develop`
- **Purpose**: Basic build and test validation
- **No deployment**: Just verifies code compiles and tests pass

---

## Monitoring

### Watch Workflows
- **GitHub Actions**: https://github.com/FlossWare/civilization-simulator-java/actions
- Filter by workflow name: CD-CI, Release, CI

### Check Releases
- **GitHub Releases**: https://github.com/FlossWare/civilization-simulator-java/releases
- **PackageCloud**: https://packagecloud.io/flossware/java (after token setup)

### Version History
```bash
# Check tags
git tag -l

# Check latest version
git describe --tags --abbrev=0

# View version bump commits
git log --all --grep="Incrementing pom.xml version"
```

---

## Benefits of This Approach

✅ **Zero manual version management** - CD-CI handles it  
✅ **Continuous deployment** - Every merge deploys automatically  
✅ **GitHub Releases** when you need them - Manual tags  
✅ **Consistent with FlossWare** - Same pattern as collections-java  
✅ **Maven-native** - No external tools (gems, CLIs)  
✅ **Automatic dependency updates** - Always latest JUnit  
✅ **Git-friendly** - Version bumps committed back to repo  

---

## Troubleshooting

### CD-CI not running
- **Check**: Did you push to `main` branch?
- **Check**: Was the push from `version-bump@flossware.org`? (skipped to avoid loops)
- **Fix**: Merge PR or push directly to main

### Release workflow not running
- **Check**: Did you push a tag in format `v*.*`?
- **Fix**: `git push origin v1.5`

### PackageCloud deployment fails
- **Error**: "Authentication failed"
- **Fix**: Add `PACKAGECLOUD_TOKEN` secret to GitHub repository settings

### Version conflict
- **Error**: "Version already exists"
- **Fix**: CD-CI auto-increments, so this shouldn't happen. If it does, manually bump in pom.xml before pushing.

---

## Migration Notes

This system replaces the original manual version-bump workflow. The old system required:
- Manual workflow trigger
- Separate version bump script
- Ruby gem installation

The new system is:
- Fully automated on main branch
- Maven-native
- Consistent with other FlossWare projects
- Zero manual intervention for minor releases

---

**Last Updated**: 2026-06-06  
**Borrowed From**: FlossWare collections-java project  
**Pattern**: Auto CD-CI + Manual Release
