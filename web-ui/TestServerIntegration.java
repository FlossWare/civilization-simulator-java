import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;

/**
 * Integration test to verify the streaming server works correctly
 */
public class TestServerIntegration {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting integration test for streaming server...\n");
        
        // Start server in a thread
        Thread serverThread = new Thread(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
                server.createContext("/", new SimpleServer.StaticFileHandler());
                server.setExecutor(null);
                server.start();
                System.out.println("Test server started on port 8888");
                
                // Keep server running for 10 seconds
                Thread.sleep(10000);
                server.stop(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Wait for server to start
        Thread.sleep(1000);
        
        // Test 1: Request index.html
        System.out.println("\nTest 1: Request index.html");
        byte[] response = makeRequest("http://localhost:8888/");
        if (response != null && response.length > 0) {
            System.out.println("  PASS - Received " + response.length + " bytes");
        } else {
            System.out.println("  FAIL - No response received");
        }
        
        // Test 2: Request simulator.html
        System.out.println("\nTest 2: Request simulator.html");
        response = makeRequest("http://localhost:8888/simulator.html");
        if (response != null && response.length > 0) {
            System.out.println("  PASS - Received " + response.length + " bytes");
        } else {
            System.out.println("  FAIL - No response received");
        }
        
        // Test 3: Request CSS file
        System.out.println("\nTest 3: Request CSS file");
        response = makeRequest("http://localhost:8888/css/style.css");
        if (response != null && response.length > 0) {
            System.out.println("  PASS - Received " + response.length + " bytes");
        } else {
            System.out.println("  FAIL - No response received");
        }
        
        // Test 4: Request nonexistent file (should be 404)
        System.out.println("\nTest 4: Request nonexistent file");
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8888/notfound.html").openConnection();
            int status = conn.getResponseCode();
            if (status == 404) {
                System.out.println("  PASS - Got 404 as expected");
            } else {
                System.out.println("  FAIL - Got " + status + " instead of 404");
            }
        } catch (Exception e) {
            System.out.println("  FAIL - " + e.getMessage());
        }
        
        System.out.println("\nIntegration tests completed!");
    }
    
    private static byte[] makeRequest(String urlString) throws Exception {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            int status = conn.getResponseCode();
            
            if (status == 200) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                try (InputStream is = conn.getInputStream()) {
                    while ((length = is.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                }
                return result.toByteArray();
            } else {
                System.out.println("  Got status: " + status);
                return null;
            }
        } catch (Exception e) {
            System.out.println("  Exception: " + e.getMessage());
            return null;
        }
    }
}
