package uz.dilshodlatipov.secretsguard.service;

import uz.dilshodlatipov.secretsguard.model.RegexDefinition;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexScanner {

    private final List<CompiledRegex> compiledRegexes;

    public RegexScanner(List<RegexDefinition> definitions) {
        this.compiledRegexes = definitions.stream()
                .map(definition -> new CompiledRegex(definition, Pattern.compile(definition.pattern())))
                .collect(Collectors.toList());
    }

    public List<CompiledRegex> getCompiledRegexes() {
        return compiledRegexes;
    }

    public record CompiledRegex(RegexDefinition definition, Pattern pattern) {
    }
}
