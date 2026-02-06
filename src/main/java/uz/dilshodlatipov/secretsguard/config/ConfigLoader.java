package uz.dilshodlatipov.secretsguard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    public static SecretsGuardProperties load(Path configPath) {
        SecretsGuardProperties defaults = new SecretsGuardProperties();
        if (configPath == null || !Files.exists(configPath)) {
            return defaults;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            JsonNode root = mapper.readTree(inputStream);
            JsonNode sg = root.path("secrets-guard");
            if (sg.isMissingNode()) {
                return defaults;
            }
            return mapper.readerForUpdating(defaults).readValue(sg);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read config: " + configPath, ex);
        }
    }
}
