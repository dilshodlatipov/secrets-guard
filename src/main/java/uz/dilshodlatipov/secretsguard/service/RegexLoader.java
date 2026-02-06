package uz.dilshodlatipov.secretsguard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import uz.dilshodlatipov.secretsguard.model.RegexDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class RegexLoader {

    public List<RegexDefinition> load(String location) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = open(location)) {
            if (inputStream == null) {
                return Collections.emptyList();
            }
            return mapper.readValue(inputStream, new TypeReference<>() {});
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load regex definitions from " + location, ex);
        }
    }

    private InputStream open(String location) throws IOException {
        if (location.startsWith("classpath:")) {
            String classpathLocation = location.substring("classpath:".length());
            return RegexLoader.class.getClassLoader().getResourceAsStream(classpathLocation);
        }
        Path path = Path.of(location);
        return Files.exists(path) ? Files.newInputStream(path) : null;
    }
}
