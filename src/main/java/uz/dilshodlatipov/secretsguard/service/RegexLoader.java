package uz.dilshodlatipov.secretsguard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uz.dilshodlatipov.secretsguard.model.RegexDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class RegexLoader {

    private final ResourceLoader resourceLoader;

    public RegexLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<RegexDefinition> load(String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            return Collections.emptyList();
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = resource.getInputStream()) {
            return mapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load regex definitions from " + location, ex);
        }
    }
}
