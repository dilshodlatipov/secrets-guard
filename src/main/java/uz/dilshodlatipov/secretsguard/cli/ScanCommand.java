package uz.dilshodlatipov.secretsguard.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import uz.dilshodlatipov.secretsguard.AppContext;
import uz.dilshodlatipov.secretsguard.model.SecretFinding;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "scan", description = "Scan a repository for secrets")
public class ScanCommand implements Callable<Integer> {

    private final AppContext context;

    @Option(names = "--path", defaultValue = ".")
    private Path path;

    @Option(names = "--staged", defaultValue = "false")
    private boolean staged;

    public ScanCommand(AppContext context) {
        this.context = context;
    }

    @Override
    public Integer call() {
        List<SecretFinding> findings = context.scanService().scan(path.toAbsolutePath().normalize(), staged);
        if (findings.isEmpty()) {
            System.out.println("No secrets detected.");
            return 0;
        }
        System.err.println("Detected " + findings.size() + " potential secret(s).\n\n" + formatFindings(findings));
        return 2;
    }

    public static String formatFindings(List<SecretFinding> findings) {
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
