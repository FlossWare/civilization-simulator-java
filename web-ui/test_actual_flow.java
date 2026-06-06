import java.nio.file.*;
import java.io.*;

public class test_actual_flow {
    private static final String STATIC_DIR = "static";
    
    public static void main(String[] args) throws IOException {
        // Simulate what happens when URI.getPath() returns "/../pom.xml"
        // (which is what happens when the request is "/..%2Fpom.xml")
        String decodedPath = "/../pom.xml";
        
        System.out.println("Input path (after URI decode): " + decodedPath);
        File result = resolveSecurePath(decodedPath);
        
        if (result == null) {
            System.out.println("BLOCKED - Path traversal detected");
        } else {
            System.out.println("ALLOWED - File path: " + result.getAbsolutePath());
            System.out.println("File exists: " + result.exists());
        }
    }
    
    private static File resolveSecurePath(String requestedPath) throws IOException {
        Path staticPath = Paths.get(STATIC_DIR).toAbsolutePath().normalize();
        String cleanPath = requestedPath.startsWith("/") ? requestedPath.substring(1) : requestedPath;
        Path requestedFilePath = staticPath.resolve(cleanPath).normalize();
        
        System.out.println("Static path: " + staticPath);
        System.out.println("Clean path: " + cleanPath);
        System.out.println("Requested file path: " + requestedFilePath.toAbsolutePath());
        System.out.println("Starts with static? " + requestedFilePath.toAbsolutePath().startsWith(staticPath));
        
        if (!requestedFilePath.toAbsolutePath().startsWith(staticPath)) {
            return null;
        }
        return requestedFilePath.toFile();
    }
}
