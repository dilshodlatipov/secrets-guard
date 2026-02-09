package uz.dilshodlatipov.secretsguard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.dilshodlatipov.secretsguard.model.HistoryFinding;
import uz.dilshodlatipov.secretsguard.model.SecretFinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GitHistoryService {

    private static final Logger log = LoggerFactory.getLogger(GitHistoryService.class);

    private final ScanService scanService;

    public GitHistoryService(ScanService scanService) {
        this.scanService = scanService;
    }

    public List<HistoryFinding> scanHistory(Path repoRoot) {
        List<HistoryFinding> findings = new ArrayList<>();
        List<String> commits = runGit(repoRoot, "rev-list", "--all");
        for (String commit : commits) {
            if (commit.isBlank()) {
                continue;
            }
            findings.addAll(scanCommit(repoRoot, commit.trim()));
        }
        return findings;
    }

    private List<HistoryFinding> scanCommit(Path repoRoot, String commitId) {
        List<HistoryFinding> findings = new ArrayList<>();
        List<String> files = runGit(repoRoot, "diff-tree", "--no-commit-id", "--name-only", "-r", commitId);
        Set<String> uniqueFiles = new HashSet<>(files);
        for (String file : uniqueFiles) {
            if (file.isBlank()) {
                continue;
            }
            String content = runGitSingle(repoRoot, "show", commitId + ":" + file);
            if (content == null) {
                continue;
            }
            List<SecretFinding> matches = scanService.scanContent(Path.of(file), content);
            for (SecretFinding match : matches) {
                findings.add(new HistoryFinding(commitId, match));
            }
        }
        return findings;
    }

    private List<String> runGit(Path repoRoot, String... args) {
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.command().add(0, "git");
            builder.directory(repoRoot.toFile());
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("git {} returned non-zero exit code {}", String.join(" ", args), exitCode);
                return List.of();
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return List.of(output.split("\n"));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Failed to execute git command", ex);
            return List.of();
        }
    }

    private String runGitSingle(Path repoRoot, String... args) {
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.command().add(0, "git");
            builder.directory(repoRoot.toFile());
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return null;
            }
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.debug("Failed to read git content", ex);
            return null;
        }
    }
}
