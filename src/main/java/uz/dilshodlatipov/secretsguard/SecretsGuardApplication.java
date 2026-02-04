package uz.dilshodlatipov.secretsguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.core.command.annotation.EnableCommand;
import uz.dilshodlatipov.secretsguard.cli.ConfigCommand;
import uz.dilshodlatipov.secretsguard.cli.InitCommand;
import uz.dilshodlatipov.secretsguard.cli.ScanCommand;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableCommand(value = {ConfigCommand.class, InitCommand.class, ScanCommand.class})
public class SecretsGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretsGuardApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("sg-scanner-");
        executor.initialize();
        return executor;
    }

}
