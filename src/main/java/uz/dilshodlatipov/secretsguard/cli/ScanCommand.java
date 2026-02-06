package uz.dilshodlatipov.secretsguard.cli;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import uz.dilshodlatipov.secretsguard.model.SecretFinding;
import uz.dilshodlatipov.secretsguard.service.ScanService;

import java.nio.file.Path;
import java.util.List;

@Component
public class ScanCommand {

    private final ScanService scanService;

    public ScanCommand(ScanService scanService) {
        this.scanService = scanService;
    }

    @Command(name = "scan", description = "Scan a repository for secrets")
    public String scan(@Option(longName = "path", defaultValue = ".") Path path,
                       @Option(longName = "staged", defaultValue = "false") boolean staged) {
        List<SecretFinding> findings = scanService.scan(path.toAbsolutePath().normalize(), staged);
        if (findings.isEmpty()) {
            return "No secrets detected.";
        }
        String message = "Detected " + findings.size() + " potential secret(s).\n\n" + formatFindings(findings);
        System.err.println(message);
        System.exit(2);
        return message;
    }

    static String formatFindings(List<SecretFinding> findings) {
        StringBuilder builder = new StringBuilder();
        for (SecretFinding finding : findings) {
            builder.append("- ")
                    .append(finding.file())
                    .append(":")
                    .append(finding.line())
                    .append(" [")
                    .append(finding.regexId())
                    .append("] ")
                    .append(finding.description())
                    .append(" -> ")
                    .append(finding.match())
                    .append("\n");
        }
        return builder.toString();
    }
}
