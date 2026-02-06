package uz.dilshodlatipov.secretsguard.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import uz.dilshodlatipov.secretsguard.AppContext;
import uz.dilshodlatipov.secretsguard.model.SecretFinding;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "init", description = "Install pre-commit hook and run initial scan")
public class InitCommand implements Callable<Integer> {

    private final AppContext context;

    @Option(names = "--path", defaultValue = ".")
    private Path path;

    @Option(names = "--skip-scan", defaultValue = "false")
    private boolean skipScan;

    public InitCommand(AppContext context) {
        this.context = context;
    }

    @Override
    public Integer call() {
        Path repoRoot = path.toAbsolutePath().normalize();
        if (!Files.exists(repoRoot.resolve(".git"))) {
            System.err.println("No .git directory found at " + repoRoot + ". Run from a git repository.");
            return 1;
        }

        Path hook = context.gitHookService().installPreCommitHook(repoRoot);
        System.out.println("Installed pre-commit hook at: " + hook);

        if (!skipScan) {
            List<SecretFinding> findings = context.scanService().scan(repoRoot, false);
            System.out.println("Initial scan complete. Findings: " + findings.size());
            if (!findings.isEmpty()) {
                System.out.println();
                System.out.println(ScanCommand.formatFindings(findings));
                return 2;
            }
        }
        return 0;
    }
}
