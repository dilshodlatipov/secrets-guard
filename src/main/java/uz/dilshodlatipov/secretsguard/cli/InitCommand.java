package uz.dilshodlatipov.secretsguard.cli;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import uz.dilshodlatipov.secretsguard.model.SecretFinding;
import uz.dilshodlatipov.secretsguard.service.GitHookService;
import uz.dilshodlatipov.secretsguard.service.ScanService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class InitCommand {

    private final GitHookService gitHookService;
    private final ScanService scanService;

    public InitCommand(GitHookService gitHookService, ScanService scanService) {
        this.gitHookService = gitHookService;
        this.scanService = scanService;
    }

    @Command(name = "init", description = "Install the pre-commit hook and scan the repo")
    public String init(@Option(longName = "path", defaultValue = ".") Path path,
                       @Option(longName = "skip-scan", defaultValue = "false") boolean skipScan) {
        Path repoRoot = path.toAbsolutePath().normalize();
        if (!Files.exists(repoRoot.resolve(".git"))) {
            return "No .git directory found at " + repoRoot + ". Run from a git repository.";
        }
        Path hook = gitHookService.installPreCommitHook(repoRoot);
        StringBuilder output = new StringBuilder("Installed pre-commit hook at: ").append(hook);
        if (!skipScan) {
            List<SecretFinding> findings = scanService.scan(repoRoot, false);
            output.append("\nInitial scan complete. Findings: ").append(findings.size());
            if (!findings.isEmpty()) {
                output.append("\n\n").append(ScanCommand.formatFindings(findings));
            }
        }
        return output.toString();
    }
}
