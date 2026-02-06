package uz.dilshodlatipov.secretsguard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;
import uz.dilshodlatipov.secretsguard.model.RegexDefinition;

@Component
public class AiScanner {

    private static final Logger log = LoggerFactory.getLogger(AiScanner.class);

    private final SecretsGuardProperties properties;
    private final AiModelService modelService;

    public AiScanner(SecretsGuardProperties properties, AiModelService modelService) {
        this.properties = properties;
        this.modelService = modelService;
    }

    public boolean isSecret(String candidate, RegexDefinition definition) {
        if (!properties.getAi().isEnabled()) {
            return true;
        }
        if (definition.skipAi() || properties.getAi().getSkipRegexIds().contains(definition.id())) {
            return true;
        }
        try {
            double score = modelService.score(candidate);
            return score >= properties.getAi().getScoreThreshold();
        } catch (Exception ex) {
            log.warn("AI detection failed; treating match as secret.", ex);
            return true;
        }
    }
}
