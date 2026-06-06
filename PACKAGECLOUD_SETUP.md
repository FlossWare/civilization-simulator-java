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

### 2. Verify Repository Configuration

The release workflow uses Maven deploy to push to PackageCloud. The repository is configured in `pom.xml`:

```xml
<distributionManagement>
    <repository>
        <id>packagecloud-flossware</id>
        <name>packagecloud-flossware</name>
        <url>https://packagecloud.io/flossware/java/maven2/</url>
    </repository>
</distributionManagement>
```

If your PackageCloud organization or repository name is different, update the URL in `pom.xml`.

## Creating a PackageCloud Repository

If you haven't created the repository yet:

1. Go to https://packagecloud.io/repos/new
2. Set repository name: `civilization-simulator`
3. Choose visibility: **Public** or **Private**
4. Click **Create Repository**

## Manual Deployment (Optional)

To deploy manually from your local machine:

### Option 1: Maven Deploy (Recommended)

```bash
# Build the project
mvn clean package

# Deploy to PackageCloud (requires PACKAGECLOUD_TOKEN in Maven settings)
mvn deploy -DskipTests
```

### Option 2: PackageCloud CLI

```bash
# Install packagecloud CLI
gem install package_cloud

# Set token
export PACKAGECLOUD_TOKEN=your_token_here

# Deploy JAR
package_cloud push flossware/java target/civilization-simulator-java-1.0.jar
```

## Automated Release Processes

This project has two automated workflows:

### 1. CD-CI Workflow (Auto-bump on main branch push)

Triggered automatically on every push to `main` branch:
- Auto-increments minor version (1.0 → 1.1)
- Updates dependencies to latest versions
- Builds and tests
- Deploys to PackageCloud
- Creates git tag
- Commits version bump back to main

### 2. Release Workflow (Manual tag-based)

Triggered by creating version tags (`v1.0`, `v2.0`, etc.):
- Builds and tests at specific version
- Creates GitHub Release with artifacts
- Deploys to PackageCloud
- Updates `latest` tag

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
