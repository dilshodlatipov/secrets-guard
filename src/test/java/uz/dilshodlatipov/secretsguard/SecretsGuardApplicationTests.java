package uz.dilshodlatipov.secretsguard;

import org.junit.jupiter.api.Test;
import uz.dilshodlatipov.secretsguard.config.ConfigLoader;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretsGuardApplicationTests {

    @Test
    void loadsDefaultConfigWhenFileDoesNotExist() {
        SecretsGuardProperties properties = ConfigLoader.load(Path.of("not-existing.yaml"));
        assertEquals("classpath:regexes.yaml", properties.getRegexesPath());
        assertTrue(properties.getAi().isEnabled());
    }
}
