package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Read JSON configuration files for network topology and traffic streams
public class NetworkLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static NetworkConfig loadTopology(String fileName) throws IOException {
        try (InputStream is = openConfigStream(fileName)) {
            if (is == null) {
                throw new IOException("Network topology file not found: " + fileName);
            }
            return mapper.readValue(is, NetworkConfig.class);
        }
    }

    public static TrafficConfig loadTraffic(String fileName) throws IOException {
        try (InputStream is = openConfigStream(fileName)) {
            if (is == null) {
                throw new IOException("Traffic scenarios file not found: " + fileName);
            }
            return mapper.readValue(is, TrafficConfig.class);
        }
    }

    private static InputStream openConfigStream(String fileName) throws IOException {
        Path filePath = Paths.get(fileName);
        if (Files.exists(filePath)) {
            return Files.newInputStream(filePath);
        }
        return NetworkLoader.class.getClassLoader().getResourceAsStream("config/" + fileName);
    }
}
