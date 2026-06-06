import java.net.*;

public class test_uri_decode {
    public static void main(String[] args) throws Exception {
        // Test what URI.getPath() returns for URL-encoded sequences
        String[] uris = {
            "http://localhost:8080/..%2F..%2Fpom.xml",
            "http://localhost:8080/..%2FSimpleServer.java",
            "http://localhost:8080/../etc/passwd"
        };
        
        for (String uriString : uris) {
            URI uri = new URI(uriString);
            System.out.println("Original: " + uriString);
            System.out.println("getPath(): " + uri.getPath());
            System.out.println();
        }
    }
}
