package org.flossware.civilization.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.flossware.civilization.engine.MonteCarloRunner;
import org.flossware.civilization.engine.SimulationEngine;
import org.flossware.civilization.engine.SimulationResult;
import org.flossware.civilization.model.Scenario;
import org.flossware.civilization.model.SimulationRules;
import org.flossware.civilization.scenarios.ScenarioRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class WebServer {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;

    private final int port;
    private final String staticDir;
    private final String corsOrigin;
    private final ObjectMapper mapper;
    private HttpServer server;

    public WebServer(int port, String staticDir) {
        this(port, staticDir, "*");
    }

    public WebServer(int port, String staticDir, String corsOrigin) {
        this.port = port;
        this.staticDir = staticDir;
        this.corsOrigin = corsOrigin;
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/scenarios", new ScenariosHandler());
        server.createContext("/api/simulate", new SimulateHandler());
        server.createContext("/api/monte-carlo", new MonteCarloHandler());
        server.createContext("/", new StaticFileHandler(staticDir));

        server.setExecutor(null);
        server.start();

        System.out.println("=".repeat(60));
        System.out.println("Civilization Simulator Web UI");
        System.out.println("=".repeat(60));
        System.out.println("Server running at: http://localhost:" + port);
        System.out.println("Static files from: " + staticDir);
        System.out.println("Press Ctrl+C to stop");
        System.out.println("=".repeat(60));
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            ObjectNode json = mapper.createObjectNode();
            json.put("status", "ok");
            json.put("mode", "backend");
            sendJson(exchange, 200, json);
        }
    }

    private class ScenariosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            sendJson(exchange, 200, mapper.valueToTree(ScenarioRegistry.listAll()));
        }
    }

    private class SimulateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                long seed = 12345L;
                String scenarioId = null;
                if (!body.isBlank()) {
                    ObjectNode reqNode = (ObjectNode) mapper.readTree(body);
                    if (reqNode.has("seed") && !reqNode.get("seed").isNull()) {
                        try {
                            seed = Long.parseLong(reqNode.get("seed").asText());
                        } catch (NumberFormatException e) {
                            sendError(exchange, 400, "Invalid seed: must be a valid integer");
                            return;
                        }
                    }
                    if (reqNode.has("scenario") && !reqNode.get("scenario").isNull()) {
                        scenarioId = reqNode.get("scenario").asText();
                    }
                }

                Scenario scenario;
                try {
                    scenario = ScenarioRegistry.getOrDefault(scenarioId);
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, e.getMessage());
                    return;
                }

                long startTime = System.nanoTime();
                SimulationEngine engine = new SimulationEngine(scenario, seed);
                SimulationResult result = engine.runWithSnapshots(0, 50);
                double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;

                ObjectNode response = mapper.createObjectNode();
                response.set("finalState", mapper.valueToTree(result.finalState()));
                response.set("events", mapper.valueToTree(result.events()));
                response.set("snapshots", mapper.valueToTree(result.snapshots()));
                response.put("durationMs", durationMs);
                response.put("techTreeSize", scenario.techTree().size());
                response.put("seed", seed);
                response.put("scenarioId", scenarioId != null ? scenarioId : "rome");

                sendJson(exchange, 200, response);
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    private class MonteCarloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                int numRuns = 50;
                long baseSeed = 12345L;
                String scenarioId = null;
                if (!body.isBlank()) {
                    ObjectNode reqNode = (ObjectNode) mapper.readTree(body);
                    if (reqNode.has("numRuns") && !reqNode.get("numRuns").isNull()) {
                        numRuns = reqNode.get("numRuns").asInt();
                        if (numRuns < 1 || numRuns > 200) {
                            sendError(exchange, 400, "numRuns must be between 1 and 200");
                            return;
                        }
                    }
                    if (reqNode.has("baseSeed") && !reqNode.get("baseSeed").isNull()) {
                        try {
                            baseSeed = Long.parseLong(reqNode.get("baseSeed").asText());
                        } catch (NumberFormatException e) {
                            sendError(exchange, 400, "Invalid baseSeed: must be a valid integer");
                            return;
                        }
                    }
                    if (reqNode.has("scenario") && !reqNode.get("scenario").isNull()) {
                        scenarioId = reqNode.get("scenario").asText();
                    }
                }

                Scenario baseScenario;
                try {
                    baseScenario = ScenarioRegistry.getOrDefault(scenarioId);
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, e.getMessage());
                    return;
                }
                SimulationRules originalRules = baseScenario.simulationRules();
                SimulationRules newRules = new SimulationRules(
                    originalRules.timeStep(),
                    originalRules.deterministicReproducible(),
                    baseSeed,
                    numRuns,
                    originalRules.parallelThreads()
                );
                Scenario scenario = new Scenario(
                    baseScenario.scenarioId(),
                    baseScenario.name(),
                    baseScenario.description(),
                    baseScenario.startYear(),
                    baseScenario.endYear(),
                    baseScenario.initialState(),
                    baseScenario.techTree(),
                    baseScenario.worldConstraints(),
                    newRules
                );

                long totalStart = System.nanoTime();

                ArrayNode runsArray = mapper.createArrayNode();
                List<SimulationResult> allResults = new ArrayList<>();

                for (int i = 0; i < numRuns; i++) {
                    long runStart = System.nanoTime();
                    SimulationEngine engine = new SimulationEngine(scenario, baseSeed);
                    SimulationResult result = engine.run(i);
                    double runDurationMs = (System.nanoTime() - runStart) / 1_000_000.0;
                    allResults.add(result);

                    ObjectNode runNode = mapper.createObjectNode();
                    runNode.put("runIndex", i);
                    runNode.put("seed", baseSeed);
                    runNode.put("population", result.finalState().population().population());
                    runNode.put("wealth", result.finalState().economy().wealth());
                    runNode.put("techCount", result.finalState().technology().unlockedTechs().size());
                    runNode.put("stability", result.finalState().politics().stability());
                    runNode.put("eventCount", result.events().size());
                    runNode.put("durationMs", runDurationMs);
                    runsArray.add(runNode);
                }

                double totalDurationMs = (System.nanoTime() - totalStart) / 1_000_000.0;

                MonteCarloRunner.MonteCarloAnalysis analysis = MonteCarloRunner.analyze(allResults);

                ObjectNode analysisNode = mapper.createObjectNode();
                analysisNode.put("totalRuns", analysis.totalRuns());
                analysisNode.put("avgPopulation", analysis.avgPopulation());
                analysisNode.put("avgWealth", analysis.avgWealth());
                analysisNode.put("avgTechs", analysis.avgTechs());
                analysisNode.put("survivalRate", analysis.survivalRate());

                ObjectNode response = mapper.createObjectNode();
                response.set("analysis", analysisNode);
                response.set("runs", runsArray);
                response.put("totalDurationMs", totalDurationMs);
                response.put("techTreeSize", scenario.techTree().size());
                response.put("scenarioId", scenarioId != null ? scenarioId : "rome");

                sendJson(exchange, 200, response);
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final String staticDir;

        StaticFileHandler(String staticDir) {
            this.staticDir = staticDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            java.io.File file = resolveSecurePath(path);
            if (file == null) {
                String response = "403 Forbidden";
                exchange.sendResponseHeaders(403, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(path);
                long fileSize = file.length();

                if (fileSize > MAX_FILE_SIZE) {
                    String response = "413 Payload Too Large";
                    exchange.sendResponseHeaders(413, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    return;
                }

                try (InputStream in = Files.newInputStream(file.toPath());
                     OutputStream out = exchange.getResponseBody()) {
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, fileSize);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    System.err.println("Error streaming file: " + e.getMessage());
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private java.io.File resolveSecurePath(String requestedPath) throws IOException {
            Path staticPath = Paths.get(staticDir).toAbsolutePath().normalize();
            String cleanPath = requestedPath.startsWith("/") ? requestedPath.substring(1) : requestedPath;
            String decodedPath = URLDecoder.decode(cleanPath, StandardCharsets.UTF_8);
            Path requestedFilePath = staticPath.resolve(decodedPath).normalize();

            if (!requestedFilePath.toAbsolutePath().startsWith(staticPath)) {
                return null;
            }

            return requestedFilePath.toFile();
        }

        private static String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            if (path.endsWith(".woff2")) return "font/woff2";
            if (path.endsWith(".woff")) return "font/woff";
            return "text/plain";
        }
    }

    private boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", corsOrigin);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object json) throws IOException {
        byte[] responseBytes = mapper.writeValueAsBytes(json);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("error", message != null ? message : "Internal server error");
        sendJson(exchange, statusCode, errorNode);
    }
}
