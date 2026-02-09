package uz.dilshodlatipov.secretsguard;

import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;
import uz.dilshodlatipov.secretsguard.service.AiModelService;
import uz.dilshodlatipov.secretsguard.service.AiScanner;
import uz.dilshodlatipov.secretsguard.service.GitHistoryService;
import uz.dilshodlatipov.secretsguard.service.GitHookService;
import uz.dilshodlatipov.secretsguard.service.RegexLoader;
import uz.dilshodlatipov.secretsguard.service.ScanService;

public class AppContext {

    private final SecretsGuardProperties properties;
    private final AiModelService aiModelService;
    private final ScanService scanService;
    private final GitHookService gitHookService;
    private final GitHistoryService gitHistoryService;

    public AppContext(SecretsGuardProperties properties) {
        this.properties = properties;
        RegexLoader regexLoader = new RegexLoader();
        this.aiModelService = new AiModelService(properties);
        AiScanner aiScanner = new AiScanner(properties, aiModelService);
        this.scanService = new ScanService(properties, regexLoader, aiScanner);
        this.gitHookService = new GitHookService(properties);
        this.gitHistoryService = new GitHistoryService(scanService);
    }

    public SecretsGuardProperties properties() { return properties; }
    public AiModelService aiModelService() { return aiModelService; }
    public ScanService scanService() { return scanService; }
    public GitHookService gitHookService() { return gitHookService; }
    public GitHistoryService gitHistoryService() { return gitHistoryService; }
}
