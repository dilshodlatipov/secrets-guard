# secrets-guard (sg-cli)

`sg-cli` is a Picocli-based CLI application for scanning repositories for leaked secrets before commit.

Detection strategy:
1. Regex scan over files.
2. Optional ONNX AI filter to reduce false positives.

## Requirements

- Java 17+
- Maven 3.9+
- Git
- (Optional) GraalVM 23+ for native image build

## Build

```bash
./mvnw clean package
```

## Run

```bash
java -jar target/secrets-guard-0.1.0.jar scan --path .
```

You can use the default config (`application.yaml`) or pass a custom config file:

```bash
java -jar target/secrets-guard-0.1.0.jar --config ./application.yaml scan --staged
```

## Commands

### Scan

```bash
java -jar target/secrets-guard-0.1.0.jar scan --path .
java -jar target/secrets-guard-0.1.0.jar scan --path . --staged
```

Exit codes:
- `0` no secrets detected
- `2` secrets detected
- `1` command/setup error

### Init (install git pre-commit hook)

```bash
java -jar target/secrets-guard-0.1.0.jar init --path .
```

Installs `.git/hooks/pre-commit` to run:

```sh
sg-cli scan --staged
```

### Show active config

```bash
java -jar target/secrets-guard-0.1.0.jar config
```

## Configuration

`application.yaml` format:

```yaml
secrets-guard:
  regexes-path: classpath:regexes.yaml
  ai:
    enabled: true
    model-path: classpath:model/model.onnx
    tokenizer-path: classpath:model
    score-threshold: 0.5
    skip-regex-ids:
      - spring-datasource-password
  scan:
    max-file-size-bytes: 1048576
    ignore:
      - .git
      - target
  hooks:
    pre-commit-command: sg-cli scan --staged
```

## GraalVM Native Image

Build native executable:

```bash
./mvnw -Pnative native:compile
```

Output binary (Linux/macOS):

```bash
./target/secrets-guard
```

Then use:

```bash
./target/secrets-guard scan --path .
```

## Notes

- ONNX model files are expected under `src/main/resources/model/`.
- Regex rules are loaded from `src/main/resources/regexes.yaml` by default.
