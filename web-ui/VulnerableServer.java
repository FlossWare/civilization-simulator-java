import java.io.*;
import java.nio.file.*;

public class VulnerableServer {
    private static final String STATIC_DIR = "static";
    
    public static void main(String[] args) throws IOException {
        // Simulate the VULNERABLE code from the committed version
        String[] attacks = {
            "/../../pom.xml",  // What "..%2F..%2Fpom.xml" becomes after URI decode
            "/../SimpleServer.java"  // What "..%2FSimpleServer.java" becomes
        };
        
        for (String path : attacks) {
            System.out.println("\n=== Attack: " + path + " ===");
            
            // VULNERABLE CODE (current HEAD)
            File file = new File(STATIC_DIR + path);
            System.out.println("Resolved to: " + file.getPath());
            System.out.println("Absolute path: " + file.getAbsolutePath());
            System.out.println("File exists: " + file.exists());
            
            if (file.exists()) {
                System.out.println("VULNERABILITY CONFIRMED - File accessible outside static/");
                System.out.println("File size: " + file.length() + " bytes");
            }
        }
    }
}
