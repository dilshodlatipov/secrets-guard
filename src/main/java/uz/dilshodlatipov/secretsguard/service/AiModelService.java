package uz.dilshodlatipov.secretsguard.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class AiModelService {

    private final SecretsGuardProperties properties;
    private OrtEnvironment environment;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    public AiModelService(SecretsGuardProperties properties) {
        this.properties = properties;
    }

    public synchronized boolean isLoaded() {
        return session != null && tokenizer != null;
    }

    public synchronized void ensureLoaded() {
        if (!properties.getAi().isEnabled() || isLoaded()) {
            return;
        }
        try {
            environment = OrtEnvironment.getEnvironment();
            Path modelPath = resolveToFile(properties.getAi().getModelPath(), "model", ".onnx");
            session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());

            Path tokenizerPath = resolveDirectory(properties.getAi().getTokenizerPath());
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize AI model", ex);
        }
    }

    public double score(String text) {
        ensureLoaded();
        if (!isLoaded()) {
            return 1.0;
        }
        try {
            Encoding encoding = tokenizer.encode(text);
            long[][] inputIdsBatch = new long[][]{encoding.getIds()};
            long[][] attentionMaskBatch = new long[][]{encoding.getAttentionMask()};

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(environment, inputIdsBatch));
            inputs.put("attention_mask", OnnxTensor.createTensor(environment, attentionMaskBatch));

            try (OrtSession.Result results = session.run(inputs)) {
                OnnxTensor output = (OnnxTensor) results.get(0);
                float[][] scores = (float[][]) output.getValue();
                return scores[0][0];
            } finally {
                for (OnnxTensor tensor : inputs.values()) {
                    tensor.close();
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("AI inference failed", ex);
        }
    }

    public synchronized Map<String, NodeInfo> inputInfo() {
        ensureLoaded();
        if (!isLoaded()) {
            return Map.of();
        }
        try {
            return session.getInputInfo();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read model input info", ex);
        }
    }

    private Path resolveDirectory(String location) throws IOException {
        if (!location.startsWith("classpath:")) {
            return Path.of(location);
        }
        String classpathDir = location.substring("classpath:".length());
        Path tempDir = Files.createTempDirectory("sg-tokenizer-");
        copyClasspathFile(classpathDir + "/tokenizer_config.json", tempDir.resolve("tokenizer_config.json"));
        copyClasspathFile(classpathDir + "/vocab.json", tempDir.resolve("vocab.json"));
        copyClasspathFile(classpathDir + "/merges.txt", tempDir.resolve("merges.txt"));
        copyClasspathFile(classpathDir + "/special_tokens_map.json", tempDir.resolve("special_tokens_map.json"));
        copyClasspathFile(classpathDir + "/config.json", tempDir.resolve("config.json"));
        tempDir.toFile().deleteOnExit();
        return tempDir;
    }

    private Path resolveToFile(String location, String prefix, String suffix) throws IOException {
        if (!location.startsWith("classpath:")) {
            return Path.of(location);
        }
        String classpathLocation = location.substring("classpath:".length());
        Path tempFile = Files.createTempFile(prefix, suffix);
        try (InputStream inputStream = getClasspathStream(classpathLocation)) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    private void copyClasspathFile(String classpathLocation, Path target) throws IOException {
        try (InputStream in = getClasspathStream(classpathLocation)) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        target.toFile().deleteOnExit();
    }

    private InputStream getClasspathStream(String classpathLocation) {
        InputStream inputStream = AiModelService.class.getClassLoader().getResourceAsStream(classpathLocation);
        if (inputStream == null) {
            throw new IllegalStateException("Classpath resource not found: " + classpathLocation);
        }
        return inputStream;
    }
}
