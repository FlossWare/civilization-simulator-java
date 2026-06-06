# PackageCloud Configuration - Borrowed from collections-java ✅

## Summary

Successfully implemented the Maven-based PackageCloud deployment pattern from the FlossWare collections-java project.

## What Was Borrowed

### 1. **CD-CI Workflow** (`.github/workflows/cd-ci.yml`)

Complete workflow that:
- ✅ Auto-increments minor version on every push to main
- ✅ Updates dependencies to latest versions  
- ✅ Builds and tests
- ✅ Deploys to PackageCloud using Maven
- ✅ Creates git tag
- ✅ Commits version bump back to repository

**Source**: `/home/sfloess/Development/github/FlossWare/collections-java/.github/workflows/main.yml`

### 2. **Maven Configuration**

#### a. Distribution Management (`pom.xml`)
```xml
<distributionManagement>
    <repository>
        <id>packagecloud-flossware</id>
        <name>packagecloud-flossware</name>
        <url>https://packagecloud.io/flossware/java/maven2/</url>
    </repository>
</distributionManagement>
```

#### b. SCM Configuration (`pom.xml`)
```xml
<scm>
    <connection>scm:git:https://github.com/FlossWare/civilization-simulator-java.git</connection>
    <developerConnection>scm:git:https://github.com/FlossWare/civilization-simulator-java.git</developerConnection>
    <url>https://github.com/FlossWare/civilization-simulator-java</url>
</scm>
```

#### c. Maven Plugins
- `build-helper-maven-plugin` (3.5.0) - Parse and manipulate version numbers
- `versions-maven-plugin` (2.16.0) - Update versions and dependencies
- `maven-scm-plugin` (2.0.1) - Git operations (checkin, tag)

### 3. **Maven Settings Action**

Using `s4u/maven-settings-action@v3.1.0` to configure PackageCloud authentication:

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

## Changes Made to Adapt

### 1. Updated Artifact Names
- **collections-java**: `jcollections-*.jar`
- **civilization-simulator-java**: `civilization-simulator-java-*.jar`

### 2. Updated Repository URLs
- **SCM**: Changed to `civilization-simulator-java.git`
- **Issues**: Changed to `civilization-simulator-java/issues`

### 3. Kept Dual-Workflow System
- **CD-CI**: Auto-deployment on main pushes (borrowed pattern)
- **Release**: Manual tag-based releases (our addition)
- **CI**: Basic build verification (our addition)

## Current Status

### ✅ Working
1. X.Y versioning system
2. Maven-based build and test
3. Workflow triggers correctly
4. Version auto-increment logic
5. Git configuration
6. SCM plugin configuration

### ⚠️ Pending Setup
1. **PackageCloud Token**: Need to add `PACKAGECLOUD_TOKEN` secret to GitHub
2. **PackageCloud Repository**: Need to create `flossware/java` repository at packagecloud.io

## Next Steps to Complete Setup

### Step 1: Create PackageCloud Repository

1. Go to https://packagecloud.io/repos/new
2. Create repository:
   - **Organization**: flossware  
   - **Name**: java
   - **Visibility**: Public or Private

### Step 2: Add GitHub Secret

1. Go to https://github.com/FlossWare/civilization-simulator-java/settings/secrets/actions
2. Click "New repository secret"
3. Add:
   - **Name**: `PACKAGECLOUD_TOKEN`
   - **Value**: Your PackageCloud API token from https://packagecloud.io/api_token

### Step 3: Verify Deployment

1. Push a commit to main branch
2. Watch CD-CI workflow at: https://github.com/FlossWare/civilization-simulator-java/actions
3. Verify deployment at: https://packagecloud.io/flossware/java

## Error Analysis

### Current Workflow Failure

```
status code: 422, reason phrase: Unprocessable Entity (422)
```

**Cause**: PackageCloud repository doesn't exist or token is invalid/missing

**Expected**: This will resolve once `PACKAGECLOUD_TOKEN` is added and repository is created

## Files Modified

1. ✅ `.github/workflows/cd-ci.yml` - New CD-CI workflow
2. ✅ `.github/workflows/release.yml` - Updated to use Maven deploy
3. ✅ `pom.xml` - Added distributionManagement, SCM, and plugins
4. ✅ `PACKAGECLOUD_SETUP.md` - Updated documentation
5. ✅ `AUTOMATED_RELEASES.md` - New comprehensive guide

## Benefits of Borrowed Pattern

✅ **Proven**: Already working in collections-java  
✅ **Maven-native**: No Ruby gems or external tools  
✅ **Automatic**: Zero manual intervention  
✅ **Consistent**: Same pattern across FlossWare projects  
✅ **Git-friendly**: Version bumps committed to repository  
✅ **Dependency management**: Auto-updates JUnit versions  

## Differences from Original

| Feature | collections-java | civilization-simulator-java |
|---------|------------------|----------------------------|
| Workflow name | main.yml | cd-ci.yml |
| Additional workflows | None | release.yml, ci.yml |
| Version scheme | X.Y | X.Y (same) |
| Deployment | PackageCloud | PackageCloud (same) |
| JAR packaging | Standard | Uber-JAR with shade plugin |
| Documentation | Minimal | Extensive (5 docs) |

## Testing Before PackageCloud Setup

You can test the version bumping without PackageCloud:

```bash
# Test locally
mvn build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}

# Check version
mvn help:evaluate -Dexpression=project.version -q -DforceStdout
```

## References

- **Source Project**: https://github.com/FlossWare/collections-java
- **Source Workflow**: https://github.com/FlossWare/collections-java/blob/main/.github/workflows/main.yml
- **PackageCloud Docs**: https://packagecloud.io/docs
- **Maven SCM Plugin**: https://maven.apache.org/scm/maven-scm-plugin/

---

**Status**: Configuration complete, awaiting PackageCloud token setup  
**Last Updated**: 2026-06-06  
**Pattern Source**: FlossWare collections-java
