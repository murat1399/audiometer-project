package com.odyometre.software;

import java.util.Objects;

public record AlgorithmStep(
        TestState newState,
        AlgorithmResult result
) {
    public AlgorithmStep {
        Objects.requireNonNull(newState, "newState cannot be null");
        Objects.requireNonNull(result, "result cannot be null");
    }
}
