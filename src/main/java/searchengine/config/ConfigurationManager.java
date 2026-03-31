package searchengine.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Singleton that loads settings from config.properties and exposes
 * them as typed accessors throughout the application.
 */
public class ConfigurationManager {

    private static ConfigurationManager instance;
    private final Properties properties = new Properties();

    private ConfigurationManager() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("[WARN] config.properties not found — using built-in defaults.");
            }
        } catch (IOException e) {
            System.err.println("[WARN] Could not read config.properties: " + e.getMessage());
        }
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }


    public String getRootDirectory() {
        return getProperty("index.root_directory", System.getProperty("user.home"));
    }

    public List<String> getIgnoreDirs() {
        return Arrays.asList(getProperty("index.ignore_dirs", ".git,node_modules").split(","));
    }

    public List<String> getTextExtensions() {
        return Arrays.asList(
                getProperty("index.text_extensions", "txt,md,java,py,js,html,css,xml,json").split(","));
    }

    public long getMaxFileSizeBytes() {
        return Long.parseLong(getProperty("index.max_file_size_bytes", "1048576"));
    }

    public int getMaxResults() {
        return Integer.parseInt(getProperty("search.max_results", "10"));
    }

    public int getPreviewLines() {
        return Integer.parseInt(getProperty("search.preview_lines", "3"));
    }
}
