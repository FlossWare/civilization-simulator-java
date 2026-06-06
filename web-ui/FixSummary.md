# Fix Summary: OOM Risk from Loading Entire Files into Memory

## Vulnerability Details
**Location**: web-ui/SimpleServer.java, line 60 (original code)
**Severity**: Critical
**Risk**: OutOfMemoryError on large file requests

## Original Vulnerable Code
```java
byte[] bytes = Files.readAllBytes(file.toPath());

exchange.getResponseHeaders().set("Content-Type", contentType);
exchange.sendResponseHeaders(200, bytes.length);
OutputStream os = exchange.getResponseBody();
os.write(bytes);
os.close();
```

## Root Cause
The `Files.readAllBytes()` method loads the entire file into a byte array in memory. For concurrent requests of large files, this multiplies the memory usage (500MB file × N concurrent requests = 500MB × N memory consumption), risking OutOfMemoryError.

## Solution Implemented

### 1. File Size Limit (Lines 17-18)
Added constants:
```java
private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB limit
private static final int BUFFER_SIZE = 8192; // 8KB streaming buffer
```

### 2. File Size Validation (Lines 65-72)
Before reading, check file size and reject files exceeding limit:
```java
if (fileSize > MAX_FILE_SIZE) {
    String response = "413 Payload Too Large";
    exchange.sendResponseHeaders(413, response.length());
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
    return;
}
```

### 3. Streaming Implementation (Lines 74-87)
Replace in-memory read with streaming:
```java
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
```

## Benefits

1. **Bounded Memory Usage**: Always uses only 8KB buffer, regardless of file size
2. **Concurrent Request Safety**: Multiple large file requests don't multiply memory consumption
3. **File Size Limit**: Rejects files over 100MB with 413 Payload Too Large HTTP status
4. **Resource Cleanup**: Try-with-resources ensures streams are properly closed
5. **Error Handling**: IOException handling prevents resource leaks on network errors

## Testing

### Unit Tests Passed
- Small file reading (4.3KB index.html)
- Streaming with 8KB chunks
- File size limit validation
- Large file rejection simulation (500MB file would be rejected)

### Backward Compatibility
- All existing static files continue to work
- HTTP status codes unchanged for successful requests
- Content-Type headers still sent correctly

## Deployment Notes

1. Maximum file size configurable via `MAX_FILE_SIZE` constant (default 100MB)
2. Buffer size configurable via `BUFFER_SIZE` constant (default 8KB)
3. No dependency changes
4. Server restart required to apply changes
