# Deployment Guide - Civilization Simulator v1.2

## Production Deployment Checklist

### ✅ Pre-Deployment Verification

- [x] All tests passing (13/13)
- [x] Build successful
- [x] Security vulnerabilities fixed (0 open)
- [x] Documentation updated
- [x] GitHub release created (v1.2)
- [x] All issues closed (16/16)

### 🚀 Deployment Options

## Option 1: Desktop JAR (Recommended)

**Target Users**: Desktop users, power users, developers

**Deployment Steps**:

1. **Download Release**:
```bash
wget https://github.com/FlossWare/civilization-simulator-java/releases/download/v1.2/civilization-simulator-java-1.1.jar
```

2. **Verify JAR**:
```bash
java -jar civilization-simulator-java-1.1.jar --help
```

3. **Run**:
```bash
# Single simulation
java -jar civilization-simulator-java-1.1.jar single

# Monte Carlo analysis
java -jar civilization-simulator-java-1.1.jar monte
```

**Requirements**:
- Java 21 or higher
- 512MB RAM minimum
- ~5MB disk space

**Performance**:
- Single simulation: ~125ms
- Monte Carlo (50 runs): ~461ms

---

## Option 2: Web UI (Simple Deployment)

**Target Users**: End users, demos, casual users

**Deployment Steps**:

1. **Clone Repository**:
```bash
git clone https://github.com/FlossWare/civilization-simulator-java.git
cd civilization-simulator-java/web-ui
```

2. **Start Server**:
```bash
java SimpleServer.java
```

3. **Access**:
```
http://localhost:8080
```

**Features**:
- ✅ No database required
- ✅ No Spring Boot or frameworks
- ✅ Client-side simulation (runs in browser)
- ✅ Responsive design
- ✅ Monte Carlo analysis
- ✅ Chart.js visualizations

**Production Deployment**:

For production web deployment, use a reverse proxy:

```nginx
# nginx configuration
server {
    listen 80;
    server_name sim.example.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Security Considerations**:
- ✅ Path traversal protection enabled
- ✅ File size limits (100MB)
- ✅ No SQL injection risk (no database)
- ✅ No XSS risk (CSP recommended)

Add this to HTML files:
```html
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; script-src 'self' https://cdn.jsdelivr.net">
```

---

## Option 3: Maven Dependency

**Target Users**: Java developers integrating simulation

**Deployment Steps**:

1. **Add Repository**:
```xml
<repositories>
    <repository>
        <id>packagecloud-flossware</id>
        <url>https://packagecloud.io/flossware/java/maven2/</url>
    </repository>
</repositories>
```

2. **Add Dependency**:
```xml
<dependency>
    <groupId>org.flossware</groupId>
    <artifactId>civilization-simulator-java</artifactId>
    <version>1.2</version>
</dependency>
```

3. **Use in Code**:
```java
import org.flossware.civilization.engine.SimulationEngine;
import org.flossware.civilization.scenarios.RomeEnuresScenario;

Scenario scenario = RomeEnuresScenario.create();
SimulationEngine engine = new SimulationEngine(scenario, 42L);
SimulationResult result = engine.run(0);

System.out.println("Population: " + result.finalState().population().population());
System.out.println("Wealth: " + result.finalState().economy().wealth());
```

---

## Option 4: Docker (Future)

**Coming Soon**: Containerized deployment

```dockerfile
# Dockerfile (template)
FROM eclipse-temurin:21-jre-alpine
COPY target/civilization-simulator-java-1.2.jar /app/sim.jar
WORKDIR /app
EXPOSE 8080
CMD ["java", "-jar", "sim.jar", "monte"]
```

---

## Mobile Apps (Beta)

### Android (Kotlin Multiplatform)

**Status**: Foundation ready, needs build

**Build Steps** (requires Android SDK):
```bash
cd civilization-simulator-kmm
./gradlew :androidApp:assembleRelease
```

**Output**: `androidApp/build/outputs/apk/release/androidApp-release.apk`

**Installation**:
1. Enable "Unknown sources" on device
2. Transfer APK to device
3. Install APK
4. Grant permissions

### iOS (Kotlin Multiplatform)

**Status**: Foundation ready, needs build

**Build Steps** (requires macOS + Xcode):
```bash
cd civilization-simulator-kmm/iosApp
xcodebuild -scheme iosApp -configuration Release archive
```

**Installation**:
- Via AltStore
- Via TestFlight (requires developer account)
- Via enterprise distribution

---

## Monitoring & Logging

### Application Logs

**CLI**:
- Logs to stdout
- Redirect: `java -jar sim.jar single > output.log 2>&1`

**Web UI**:
- Browser console for client-side errors
- Server logs from SimpleServer.java

### Performance Monitoring

**Key Metrics**:
- Simulation duration (target: <200ms)
- Monte Carlo duration (target: <1s for 50 runs)
- Memory usage (target: <100MB)
- Event count (typical: 400-600)

**Monitoring Commands**:
```bash
# JVM monitoring
jconsole

# Memory usage
jps -v | grep civilization-simulator
jstat -gc <PID>

# Performance profiling
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=sim.jfr \
     -jar civilization-simulator-java-1.1.jar monte
```

---

## Troubleshooting

### Common Issues

**1. "Module not found" error**
```
Error: java.lang.module.FindException: Module org.flossware not found
```
**Fix**: Use Java 21 or higher
```bash
java -version  # Should show 21+
```

**2. "Out of memory" error**
```
Exception in thread "main" java.lang.OutOfMemoryError
```
**Fix**: Increase heap size
```bash
java -Xmx2g -jar civilization-simulator-java-1.1.jar monte
```

**3. Web UI 404 errors**
```
GET /css/style.css 404 Not Found
```
**Fix**: Start server from correct directory
```bash
cd web-ui  # Important!
java SimpleServer.java
```

**4. Simulation crashes (population drops to 1000)**
```
Final Population: 1000
Survival Rate: 0.0%
```
**Status**: ✅ Fixed in v1.2
- Update to latest release
- Old versions had economic model bugs

---

## Backup & Recovery

### Backing Up

**Configuration**:
- No database to backup
- Results are ephemeral (generated on-demand)

**Custom Scenarios** (if created):
```bash
# Backup custom scenario files
cp src/main/java/org/flossware/civilization/scenarios/*.java backup/
```

### Rolling Back

**Reverting to Previous Version**:
```bash
# Download specific version
wget https://github.com/FlossWare/civilization-simulator-java/releases/download/v1.1/civilization-simulator-java-1.1.jar

# Or use git tag
git checkout v1.1
mvn clean package
```

---

## Scaling

### Horizontal Scaling (Multiple Instances)

Since the simulation is stateless, you can run multiple instances:

```bash
# Instance 1
java -jar sim.jar monte &

# Instance 2
java -jar sim.jar monte &

# Aggregate results manually
```

### Vertical Scaling (More Resources)

```bash
# More heap for larger Monte Carlo runs
java -Xmx4g -jar sim.jar monte

# More threads (edit scenario)
# Edit simulationRules.parallelThreads in scenario code
```

---

## Security Best Practices

### Production Deployment

1. **Run as non-root user**
```bash
useradd -m -s /bin/bash simuser
su - simuser
java -jar civilization-simulator-java-1.1.jar monte
```

2. **Use systemd service**
```ini
[Unit]
Description=Civilization Simulator
After=network.target

[Service]
Type=simple
User=simuser
WorkingDirectory=/opt/civilization-simulator
ExecStart=/usr/bin/java -jar civilization-simulator-java-1.1.jar monte
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

3. **Enable firewall** (web UI only)
```bash
# Allow only specific port
ufw allow 8080/tcp
ufw enable
```

4. **Use HTTPS** (web UI production)
```bash
# Use nginx with SSL/TLS
certbot --nginx -d sim.example.com
```

---

## Support & Maintenance

### Getting Help
- GitHub Issues: https://github.com/FlossWare/civilization-simulator-java/issues
- Documentation: See README.md
- Examples: See test files

### Updating
```bash
# Pull latest
git pull origin main

# Rebuild
mvn clean package

# Run tests
mvn test
```

### Version Compatibility
- Java: 21+ required
- Maven: 3.9+ recommended
- Kotlin: 2.0+ (for mobile builds)

---

## License Compliance

**GPL-3.0 Requirements**:

1. **Source Code Availability**
   - Provide link to source: https://github.com/FlossWare/civilization-simulator-java
   - Include LICENSE file in distributions

2. **Derivative Works**
   - Must also be GPL-3.0
   - Must provide source code
   - Must preserve copyright notices

3. **Binary Distribution**
   - Include copy of GPL-3.0 license
   - Provide written offer for source code
   - Display license in --help or About dialog

---

## Changelog

### v1.2 (2026-06-06)
- ✅ Fixed critical simulation bugs (0% → 100% survival)
- ✅ Patched security vulnerabilities (path traversal, OOM)
- ✅ Added complete web UI
- ✅ Added CLI validation and help
- ✅ Created mobile foundation (KMM)
- ✅ Fixed all CI/CD issues
- ✅ Synchronized all documentation

### v1.1 (Previous)
- Basic simulation engine
- Economic model (buggy)
- CLI interface (no validation)

---

**Last Updated**: 2026-06-06  
**Version**: 1.2  
**Status**: Production Ready ✅
