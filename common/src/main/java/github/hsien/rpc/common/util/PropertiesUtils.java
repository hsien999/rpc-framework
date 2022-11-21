package github.hsien.rpc.common.util;


import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * Properties utils
 *
 * @author hsien
 */
public abstract class PropertiesUtils {
    public static Properties readProperties(String fileName) {
        Properties properties;
        try {
            URI rootUri = ClassLoader.getSystemResource("").toURI();
            String rootDir = Paths.get(rootUri).toString();
            try (InputStreamReader reader = new InputStreamReader(
                Files.newInputStream(Paths.get(rootDir, fileName)), StandardCharsets.UTF_8)) {
                properties = new Properties();
                properties.load(reader);
                return properties;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read properties from file: " + fileName, e);
        }
    }
}
