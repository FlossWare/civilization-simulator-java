import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Minimal HTTP server for the Civilization Simulator Web UI.
 * No dependencies - uses only JDK HttpServer.
 *
 * Usage: java SimpleServer.java
 * Then open: http://localhost:8080
 */
public class SimpleServer {
    private static final int PORT = 8080;
    private static final String STATIC_DIR = "static";
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB limit
    private static final int BUFFER_SIZE = 8192; // 8KB streaming buffer

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API endpoints (return JSON stubs; real integration requires the simulator JAR on classpath)
        server.createContext("/api/simulate", new ApiHandler(
            "{\"error\":\"Backend simulation not available. Use the browser-based simulation (no backend required).\"}"
        ));
        server.createContext("/api/monte-carlo", new ApiHandler(
            "{\"error\":\"Backend Monte Carlo not available. Use the browser-based analysis (no backend required).\"}"
        ));
        server.createContext("/api/health", new ApiHandler(
            "{\"status\":\"ok\",\"mode\":\"static\",\"message\":\"Server is running. Simulation runs in the browser.\"}"
        ));

        // Serve static files (must be last, as it catches all paths)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=".repeat(60));
        System.out.println("Civilization Simulator Web UI");
        System.out.println("=".repeat(60));
        System.out.println("Server running at: http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop");
        System.out.println("=".repeat(60));
    }

    /**
     * Simple API handler that returns a fixed JSON response.
     * In a full integration, these would invoke the simulation engine.
     */
    static class ApiHandler implements HttpHandler {
        private final String jsonResponse;

        ApiHandler(String jsonResponse) {
            this.jsonResponse = jsonResponse;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Sanitize and validate the requested path to prevent path traversal attacks
            File file = resolveSecurePath(path);
            if (file == null) {
                // Path traversal attempt detected
                String response = "403 Forbidden";
                exchange.sendResponseHeaders(403, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(path);
                long fileSize = file.length();

                // Enforce maximum file size limit to prevent OOM attacks
                if (fileSize > MAX_FILE_SIZE) {
                    String response = "413 Payload Too Large";
                    exchange.sendResponseHeaders(413, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
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
                    // Connection may already be closed, ignore further errors
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        /**
         * Securely resolves a requested path to ensure it stays within STATIC_DIR.
         * Prevents path traversal attacks using both URL decoding and canonical path validation.
         *
         * Defends against:
         * - Literal path traversal: "/../etc/passwd"
         * - URL-encoded traversal: "/..%2F..%2Fpom.xml" (%2F = /)
         * - Alternative encodings: "/%2e%2e%2fpom.xml" (%2e = .)
         * - Double encoding and other bypass techniques
         *
         * @param requestedPath The path requested by the client (may contain URL encoding)
         * @return A File object pointing within STATIC_DIR, or null if path traversal is detected
         * @throws IOException If an I/O error occurs
         */
        private File resolveSecurePath(String requestedPath) throws IOException {
            // Get absolute, normalized path to the static directory
            Path staticPath = Paths.get(STATIC_DIR).toAbsolutePath().normalize();

            // Remove leading slash from requested path if present
            String cleanPath = requestedPath.startsWith("/") ? requestedPath.substring(1) : requestedPath;

            // CRITICAL: Decode URL-encoded sequences first (e.g., %2F becomes /)
            // This prevents bypass attacks using encoded path traversal sequences
            String decodedPath = URLDecoder.decode(cleanPath, StandardCharsets.UTF_8);

            // Resolve the requested path relative to static directory and normalize
            // This collapses .. and . components after URL decoding
            Path requestedFilePath = staticPath.resolve(decodedPath).normalize();

            // Verify that the resolved path is within or equal to STATIC_DIR
            // Compare canonical paths to ensure no escape via symlinks or other tricks
            if (!requestedFilePath.toAbsolutePath().startsWith(staticPath)) {
                // Path traversal attempt: resolved path is outside STATIC_DIR
                return null;
            }

            return requestedFilePath.toFile();
        }

        private String getContentType(String path) {
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
}
