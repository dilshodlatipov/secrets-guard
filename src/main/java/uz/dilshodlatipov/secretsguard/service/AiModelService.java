package uz.dilshodlatipov.secretsguard.service;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.microsoft.onnxruntime.OnnxTensor;
import com.microsoft.onnxruntime.OrtEnvironment;
import com.microsoft.onnxruntime.OrtSession;
import com.microsoft.onnxruntime.TensorInfo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uz.dilshodlatipov.secretsguard.config.SecretsGuardProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class AiModelService {

    private final SecretsGuardProperties properties;
    private final ResourceLoader resourceLoader;
    private OrtEnvironment environment;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    public AiModelService(SecretsGuardProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
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
            Resource modelResource = resourceLoader.getResource(properties.getAi().getModelPath());
            if (!modelResource.exists()) {
                throw new IllegalStateException("Model not found at " + properties.getAi().getModelPath());
            }
            Path modelPath = writeTempFile(modelResource, "model", ".onnx");
            session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());
            Resource tokenizerResource = resourceLoader.getResource(properties.getAi().getTokenizerPath());
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerResource.getFile().toPath());
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
            HuggingFaceTokenizer.Encoding encoding = tokenizer.encode(text);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[][] inputIdsBatch = new long[][]{inputIds};
            long[][] attentionMaskBatch = new long[][]{attentionMask};

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(environment, inputIdsBatch));
            inputs.put("attention_mask", OnnxTensor.createTensor(environment, attentionMaskBatch));

            try (OrtSession.Result results = session.run(inputs)) {
                OnnxTensor output = (OnnxTensor) results.get(0);
                float[][] scores = (float[][]) output.getValue();
                float score = scores[0][0];
                return score;
            } finally {
                for (OnnxTensor tensor : inputs.values()) {
                    tensor.close();
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("AI inference failed", ex);
        }
    }

    public synchronized Map<String, TensorInfo> inputInfo() {
        ensureLoaded();
        if (!isLoaded()) {
            return Map.of();
        }
        return session.getInputInfo();
    }

    private Path writeTempFile(Resource resource, String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}
