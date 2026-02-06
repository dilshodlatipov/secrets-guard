package uz.dilshodlatipov.secretsguard;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import uz.dilshodlatipov.secretsguard.cli.ConfigCommand;
import uz.dilshodlatipov.secretsguard.cli.InitCommand;
import uz.dilshodlatipov.secretsguard.cli.ScanCommand;
import uz.dilshodlatipov.secretsguard.config.ConfigLoader;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "sg-cli", mixinStandardHelpOptions = true, version = "sg-cli 0.1.0",
        description = "Secrets Guard CLI")
public class SecretsGuardApplication implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("Use a subcommand: scan, init, or config. Try --help.");
        return 0;
    }

    public static void main(String[] args) {
        Path configPath = extractConfigPath(args);
        SecretsGuardProperties properties = ConfigLoader.load(configPath);
        AppContext context = new AppContext(properties);

        CommandLine commandLine = new CommandLine(new SecretsGuardApplication())
                .addSubcommand("scan", new ScanCommand(context))
                .addSubcommand("init", new InitCommand(context))
                .addSubcommand("config", new ConfigCommand(context));

        int exitCode = commandLine.execute(stripConfigArgs(args));
        System.exit(exitCode);
    }

    private static Path extractConfigPath(String[] args) {
        Path defaultPath = Path.of("application.yaml");
        for (int i = 0; i < args.length; i++) {
            if (("-c".equals(args[i]) || "--config".equals(args[i])) && i + 1 < args.length) {
                return Path.of(args[i + 1]);
            }
        }
        return defaultPath;
    }

    private static String[] stripConfigArgs(String[] args) {
        if (args.length < 2) {
            return args;
        }
        for (int i = 0; i < args.length - 1; i++) {
            if ("-c".equals(args[i]) || "--config".equals(args[i])) {
                String[] newArgs = new String[args.length - 2];
                int idx = 0;
                for (int j = 0; j < args.length; j++) {
                    if (j == i || j == i + 1) {
                        continue;
                    }
                    newArgs[idx++] = args[j];
                }
                return newArgs;
            }
        }
        return args;
    }
}
