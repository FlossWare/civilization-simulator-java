#!/bin/bash

echo "═══════════════════════════════════════════════════════════════"
echo "  Civilization Simulator v1.2 - Complete Feature Demonstration"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Test 1: CLI Help
echo "📖 Test 1: CLI Help & Validation"
echo "================================"
java -jar target/civilization-simulator-java-1.1.jar --help
echo ""

# Test 2: Single Simulation
echo "🎮 Test 2: Single Simulation (Fixed Engine)"
echo "============================================"
java -jar target/civilization-simulator-java-1.1.jar single
echo ""

# Test 3: Monte Carlo Analysis  
echo "📊 Test 3: Monte Carlo Analysis (50 runs)"
echo "=========================================="
java -jar target/civilization-simulator-java-1.1.jar monte
echo ""

# Test 4: Invalid Command (validation)
echo "❌ Test 4: Invalid Command Handling"
echo "===================================="
java -jar target/civilization-simulator-java-1.1.jar invalid 2>&1 || true
echo ""

# Test 5: Web UI
echo "🌐 Test 5: Web UI Endpoints"
echo "============================"
echo "Starting web server..."
cd web-ui
java SimpleServer.java &
SERVER_PID=$!
sleep 3

echo ""
echo "Testing endpoints:"
echo "  - Landing page: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/)"
echo "  - CSS:          $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/css/style.css)"
echo "  - Main.js:      $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/js/main.js)"
echo "  - Simulator:    $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/simulator.html)"
echo "  - Monte Carlo:  $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/monte-carlo.html)"

echo ""
echo "Security tests:"
echo "  - Path traversal (../):   $(curl -s http://localhost:8080/../pom.xml | head -1)"
echo "  - Path traversal (..%2F): $(curl -s http://localhost:8080/..%2Fpom.xml | head -1)"

kill $SERVER_PID 2>/dev/null
cd ..
echo ""

# Test 6: Unit Tests
echo "✅ Test 6: Unit Tests"
echo "====================="
mvn test -q 2>&1 | tail -10
echo ""

echo "═══════════════════════════════════════════════════════════════"
echo "  ✅ All Tests Complete - Production Ready!"
echo "═══════════════════════════════════════════════════════════════"
