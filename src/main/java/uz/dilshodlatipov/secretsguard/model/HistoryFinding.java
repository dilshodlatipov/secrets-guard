package uz.dilshodlatipov.secretsguard.model;

public record HistoryFinding(
        String commitId,
        SecretFinding finding
) {
}
