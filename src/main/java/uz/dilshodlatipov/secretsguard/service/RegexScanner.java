package uz.dilshodlatipov.secretsguard.service;

import lombok.Getter;
import uz.dilshodlatipov.secretsguard.model.RegexDefinition;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class RegexScanner {

    private final List<CompiledRegex> compiledRegexes;

    public RegexScanner(List<RegexDefinition> definitions) {
        this.compiledRegexes = definitions.stream()
                .map(definition -> new CompiledRegex(definition, Pattern.compile(definition.pattern())))
                .collect(Collectors.toList());
    }

    public record CompiledRegex(RegexDefinition definition, Pattern pattern) {
    }
}
