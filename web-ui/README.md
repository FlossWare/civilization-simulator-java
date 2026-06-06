# Civilization Simulator - Web UI

Lightweight web interface for the Civilization Simulator using vanilla JavaScript and the core JAR's API.

## Architecture

**No Spring Boot Required!** This is a simple static web app that:
- Runs entirely in the browser
- Uses Web Workers for simulation execution
- Calls the civilization-simulator JAR methods directly via GraalVM/Native or WebAssembly

## Quick Start

### Option 1: Static File Server

```bash
cd web-ui/static
python3 -m http.server 8080
# Open http://localhost:8080
```

### Option 2: Simple Java HTTP Server

```bash
cd web-ui
java SimpleServer.java
# Open http://localhost:8080
```

## Features

- ✅ Single simulation execution
- ✅ Monte Carlo analysis with charts
- ✅ Real-time visualization
- ✅ Scenario configuration
- ✅ No backend required (runs in browser)

## How It Works

The web UI uses WebAssembly (WASM) compilation of the core JAR to run simulations directly in the browser, or alternatively calls a minimal REST API wrapper.

For now, it includes a tiny Java HTTP server that serves static files and provides REST endpoints.
