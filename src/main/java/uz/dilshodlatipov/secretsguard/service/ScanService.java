package uz.dilshodlatipov.secretsguard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;
import uz.dilshodlatipov.secretsguard.model.RegexDefinition;
import uz.dilshodlatipov.secretsguard.model.SecretFinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

@Service
public class ScanService {

    private static final Logger log = LoggerFactory.getLogger(ScanService.class);

    private final SecretsGuardProperties properties;
    private final RegexLoader regexLoader;
    private final AiScanner aiScanner;

    public ScanService(SecretsGuardProperties properties, RegexLoader regexLoader, AiScanner aiScanner) {
        this.properties = properties;
        this.regexLoader = regexLoader;
        this.aiScanner = aiScanner;
    }

    public List<SecretFinding> scan(Path root, boolean stagedOnly) {
        List<RegexDefinition> definitions = regexLoader.load(properties.getRegexesPath());
        RegexScanner regexScanner = new RegexScanner(definitions);
        List<Path> files = stagedOnly ? stagedFiles(root) : allFiles(root);
        List<SecretFinding> findings = new ArrayList<>();
        for (Path file : files) {
            findings.addAll(scanFile(file, regexScanner));
        }
        return findings;
    }

    private List<Path> allFiles(Path root) {
        List<Path> files = new ArrayList<>();
        try (var stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> !shouldIgnore(root, path))
                    .filter(this::withinSizeLimit)
                    .forEach(files::add);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to scan files", ex);
        }
        return files;
    }

    private List<Path> stagedFiles(Path root) {
        List<Path> files = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("git", "diff", "--name-only", "--cached")
                    .directory(root.toFile())
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("git diff --cached returned non-zero exit code {}", exitCode);
                return files;
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (String line : output.split("\n")) {
                if (!line.isBlank()) {
                    Path file = root.resolve(line.trim());
                    if (Files.isRegularFile(file) && !shouldIgnore(root, file) && withinSizeLimit(file)) {
                        files.add(file);
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Failed to read staged files", ex);
        }
        return files;
    }

    private boolean withinSizeLimit(Path file) {
        try {
            return Files.size(file) <= properties.getScan().getMaxFileSizeBytes();
        } catch (IOException ex) {
            return false;
        }
    }

    private boolean shouldIgnore(Path root, Path file) {
        Set<String> ignoreNames = new HashSet<>(properties.getScan().getIgnore());
        Path relative = root.relativize(file);
        for (Path part : relative) {
            if (ignoreNames.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private List<SecretFinding> scanFile(Path file, RegexScanner regexScanner) {
        List<SecretFinding> findings = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                for (RegexScanner.CompiledRegex compiled : regexScanner.getCompiledRegexes()) {
                    Matcher matcher = compiled.pattern().matcher(line);
                    while (matcher.find()) {
                        String match = matcher.group();
                        boolean aiConfirmed = aiScanner.isSecret(match, compiled.definition());
                        if (aiConfirmed) {
                            findings.add(new SecretFinding(
                                    file,
                                    lineNumber,
                                    match,
                                    compiled.definition().id(),
                                    compiled.definition().description(),
                                    true
                            ));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            log.warn("Failed to read {}", file, ex);
        }
        return findings;
    }
}
