package com.legalwatch.backend.infrastructure.rag;

import com.legalwatch.backend.application.rag.EmbeddingService;

import java.util.Locale;

public class FakeEmbeddingService implements EmbeddingService {

    private final int dimension;

    public FakeEmbeddingService(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("dimension must be positive");
        }
        this.dimension = dimension;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        float[] vector = new float[dimension];
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        normalized.codePoints()
                .filter(codePoint -> !Character.isWhitespace(codePoint))
                .forEach(codePoint -> vector[Math.floorMod(codePoint, dimension)] += 1.0f);
        normalize(vector);
        return vector;
    }

    private static void normalize(float[] vector) {
        double sum = 0.0;
        for (float value : vector) {
            sum += value * value;
        }
        if (sum == 0.0) {
            return;
        }

        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
