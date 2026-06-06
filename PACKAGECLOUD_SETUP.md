# PackageCloud.io Setup Guide

This document explains how to configure PackageCloud.io deployment for the Civilization Simulator project.

## Prerequisites

1. PackageCloud.io account
2. Repository created at PackageCloud: `FlossWare/civilization-simulator`
3. PackageCloud API token

## GitHub Repository Setup

### 1. Add PackageCloud Token as Secret

1. Go to your GitHub repository settings
2. Navigate to: **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add secret:
   - **Name**: `PACKAGECLOUD_TOKEN`
   - **Value**: Your PackageCloud API token (from https://packagecloud.io/api_token)

### 2. Verify Repository Name

The release workflow is configured to deploy to:
```
FlossWare/civilization-simulator/java/any/any
```

If your repository name is different, update `.github/workflows/release.yml`:

```yaml
package_cloud push YOUR_ORG/YOUR_REPO/java/any/any target/civilization-simulator-java-*.jar
```

## Creating a PackageCloud Repository

If you haven't created the repository yet:

1. Go to https://packagecloud.io/repos/new
2. Set repository name: `civilization-simulator`
3. Choose visibility: **Public** or **Private**
4. Click **Create Repository**

## Manual Deployment (Optional)

To deploy manually from your local machine:

```bash
# Install packagecloud CLI
gem install package_cloud

# Deploy JAR
package_cloud push FlossWare/civilization-simulator/java/any/any target/civilization-simulator-java-1.0.jar
```

## Automated Release Process

Once configured, releases happen automatically:

1. **Trigger Version Bump**:
   - Go to Actions → "Version Bump" workflow
   - Run workflow with major or minor bump

2. **Automatic Steps**:
   - Version bumped in all files
   - Git tag created (`v1.1`, `v2.0`, etc.)
   - Tag push triggers release workflow
   - JAR built and tested
   - GitHub Release created
   - JAR deployed to PackageCloud.io

## Verifying Deployment

After release, verify deployment:

1. Check GitHub Release: https://github.com/FlossWare/civilization-simulator-java/releases
2. Check PackageCloud: https://packagecloud.io/FlossWare/civilization-simulator

## Downloading from PackageCloud

Users can download releases from:

```bash
# Direct download
curl -O https://packagecloud.io/FlossWare/civilization-simulator/packages/java/civilization-simulator-java-1.0.jar/download

# Or configure Maven repository (future enhancement)
```

## Troubleshooting

### Release workflow fails at PackageCloud step

**Error**: `Authentication failed`
- **Fix**: Verify `PACKAGECLOUD_TOKEN` secret is set correctly

**Error**: `Repository not found`
- **Fix**: Create repository at PackageCloud.io or update repository path in workflow

**Error**: `Package already exists`
- **Fix**: PackageCloud doesn't allow duplicate versions. Delete old version or increment version number.

### Token Permissions

Your PackageCloud token needs:
- Read/Write access to repositories
- Package upload permissions

## Future Enhancements

Consider adding:
- Maven repository metadata (for `mvn` dependency resolution)
- Debian/RPM packaging (for system-level installation)
- Homebrew formula (for macOS users)
- Docker image (for containerized deployment)

## Links

- PackageCloud Documentation: https://packagecloud.io/docs
- GitHub Actions Documentation: https://docs.github.com/en/actions
- Project Repository: https://github.com/FlossWare/civilization-simulator-java
