import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
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

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Serve static files
        server.createContext("/", new StaticFileHandler());

        // API endpoints would go here
        // server.createContext("/api/simulate", new SimulateHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=".repeat(60));
        System.out.println("Civilization Simulator Web UI");
        System.out.println("=".repeat(60));
        System.out.println("Server running at: http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop");
        System.out.println("=".repeat(60));
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File(STATIC_DIR + path);
            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(path);
                byte[] bytes = Files.readAllBytes(file.toPath());

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
}
