package uz.dilshodlatipov.secretsguard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "secrets-guard")
public class SecretsGuardProperties {

    private String regexesPath = "classpath:regexes.yaml";
    private final Ai ai = new Ai();
    private final Scan scan = new Scan();
    private final Hooks hooks = new Hooks();

    public String getRegexesPath() {
        return regexesPath;
    }

    public void setRegexesPath(String regexesPath) {
        this.regexesPath = regexesPath;
    }

    public Ai getAi() {
        return ai;
    }

    public Scan getScan() {
        return scan;
    }

    public Hooks getHooks() {
        return hooks;
    }

    public static class Ai {
        private boolean enabled = true;
        private String modelPath = "classpath:model/model.onnx";
        private String tokenizerPath = "classpath:model";
        private double scoreThreshold = 0.5;
        private final List<String> skipRegexIds = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getModelPath() {
            return modelPath;
        }

        public void setModelPath(String modelPath) {
            this.modelPath = modelPath;
        }

        public String getTokenizerPath() {
            return tokenizerPath;
        }

        public void setTokenizerPath(String tokenizerPath) {
            this.tokenizerPath = tokenizerPath;
        }

        public double getScoreThreshold() {
            return scoreThreshold;
        }

        public void setScoreThreshold(double scoreThreshold) {
            this.scoreThreshold = scoreThreshold;
        }

        public List<String> getSkipRegexIds() {
            return skipRegexIds;
        }
    }

    public static class Scan {
        private long maxFileSizeBytes = 1_048_576;
        private final List<String> ignore = new ArrayList<>();

        public long getMaxFileSizeBytes() {
            return maxFileSizeBytes;
        }

        public void setMaxFileSizeBytes(long maxFileSizeBytes) {
            this.maxFileSizeBytes = maxFileSizeBytes;
        }

        public List<String> getIgnore() {
            return ignore;
        }
    }

    public static class Hooks {
        private String preCommitCommand = "sg-cli scan --staged";

        public String getPreCommitCommand() {
            return preCommitCommand;
        }

        public void setPreCommitCommand(String preCommitCommand) {
            this.preCommitCommand = preCommitCommand;
        }
    }
}
