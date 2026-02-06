package uz.dilshodlatipov.secretsguard.model;

import java.nio.file.Path;

public record SecretFinding(
        Path file,
        int line,
        String match,
        String regexId,
        String description,
        boolean aiConfirmed
) {
}
