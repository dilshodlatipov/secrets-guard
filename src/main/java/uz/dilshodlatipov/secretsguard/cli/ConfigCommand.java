package uz.dilshodlatipov.secretsguard.cli;

import picocli.CommandLine.Command;
import uz.dilshodlatipov.secretsguard.AppContext;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "config", description = "Show active configuration")
public class ConfigCommand implements Callable<Integer> {

    private final AppContext context;

    public ConfigCommand(AppContext context) {
        this.context = context;
    }

    @Override
    public Integer call() {
        SecretsGuardProperties properties = context.properties();
        String value = "Regexes path: " + properties.getRegexesPath() + "\n" +
                "AI enabled: " + properties.getAi().isEnabled() + "\n" +
                "AI model: " + properties.getAi().getModelPath() + "\n" +
                "AI tokenizer: " + properties.getAi().getTokenizerPath() + "\n" +
                "AI threshold: " + properties.getAi().getScoreThreshold() + "\n" +
                "AI skip regex IDs: " + String.join(", ", properties.getAi().getSkipRegexIds()) + "\n" +
                "Scan ignore: " + properties.getScan().getIgnore().stream().collect(Collectors.joining(", ")) + "\n" +
                "Model loaded: " + context.aiModelService().isLoaded();
        System.out.println(value);
        return 0;
    }
}
