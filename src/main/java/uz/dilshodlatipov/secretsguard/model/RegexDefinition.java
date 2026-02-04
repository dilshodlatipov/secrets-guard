package uz.dilshodlatipov.secretsguard.model;

public record RegexDefinition(
        String id,
        String description,
        String pattern,
        boolean skipAi
) {
}
