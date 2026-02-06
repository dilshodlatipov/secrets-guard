package uz.dilshodlatipov.secretsguard.cli;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;
import uz.dilshodlatipov.secretsguard.service.AiModelService;

import java.util.stream.Collectors;

@Component
public class ConfigCommand {

    private final SecretsGuardProperties properties;
    private final AiModelService aiModelService;

    public ConfigCommand(SecretsGuardProperties properties, AiModelService aiModelService) {
        this.properties = properties;
        this.aiModelService = aiModelService;
    }

    @Command(name = "config", description = "Show the active configuration")
    public String show() {
        StringBuilder builder = new StringBuilder();
        builder.append("Regexes path: ").append(properties.getRegexesPath()).append("\n")
                .append("AI enabled: ").append(properties.getAi().isEnabled()).append("\n")
                .append("AI model: ").append(properties.getAi().getModelPath()).append("\n")
                .append("AI tokenizer: ").append(properties.getAi().getTokenizerPath()).append("\n")
                .append("AI threshold: ").append(properties.getAi().getScoreThreshold()).append("\n")
                .append("AI skip regex IDs: ")
                .append(String.join(", ", properties.getAi().getSkipRegexIds()))
                .append("\n")
                .append("Scan ignore: ")
                .append(properties.getScan().getIgnore().stream().collect(Collectors.joining(", ")))
                .append("\n")
                .append("Model loaded: ").append(aiModelService.isLoaded());
        return builder.toString();
    }
}
