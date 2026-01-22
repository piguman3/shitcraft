import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Resource {
    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    static ByteBuffer getResourceFileAsByteBuffer(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;

            byte[] bytes = new byte[is.available()];
            DataInputStream dataInputStream = new DataInputStream(is);
            dataInputStream.readFully(bytes);
            
            ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length);
            directBuffer.put(bytes);
            directBuffer.flip();
            return directBuffer;
        }
    }
}
