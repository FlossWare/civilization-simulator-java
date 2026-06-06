import java.nio.file.*;
import java.io.*;

public class test_path_traversal {
    private static final String STATIC_DIR = "static";
    
    public static void main(String[] args) throws IOException {
        // Test various path traversal attempts
        String[] testPaths = {
            "/../etc/passwd",
            "/..%2F..%2Fpom.xml",
            "/..%2FSimpleServer.java",
            "/../../../etc/passwd",
            "/index.html"
        };
        
        for (String path : testPaths) {
            System.out.println("\n=== Testing: " + path + " ===");
            File result = resolveSecurePath(path);
            if (result == null) {
                System.out.println("BLOCKED (null returned)");
            } else {
                System.out.println("ALLOWED: " + result.getAbsolutePath());
            }
        }
    }
    
    private static File resolveSecurePath(String requestedPath) throws IOException {
        // Get absolute, normalized path to the static directory
        Path staticPath = Paths.get(STATIC_DIR).toAbsolutePath().normalize();

        // Remove leading slash from requested path if present
        String cleanPath = requestedPath.startsWith("/") ? requestedPath.substring(1) : requestedPath;

        // Resolve the requested path relative to static directory and normalize
        Path requestedFilePath = staticPath.resolve(cleanPath).normalize();

        // Verify that the resolved path is within or equal to STATIC_DIR
        if (!requestedFilePath.toAbsolutePath().startsWith(staticPath)) {
            // Path traversal attempt: resolved path is outside STATIC_DIR
            return null;
        }

        return requestedFilePath.toFile();
    }
}
