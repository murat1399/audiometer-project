package com.odyometre.software;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AlgorithmResult(
        StepStatus status,
        Optional<Ear> ear,
        Optional<Integer> frequencyHz,
        Optional<Integer> nextDb,
        Optional<AudiogramPoint> audiogramPoint,
        List<AudiogramPoint> allAudiogramPoints,
        String message
) {
    public AlgorithmResult {
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(ear, "ear cannot be null");
        Objects.requireNonNull(frequencyHz, "frequencyHz cannot be null");
        Objects.requireNonNull(nextDb, "nextDb cannot be null");
        Objects.requireNonNull(audiogramPoint, "audiogramPoint cannot be null");
        Objects.requireNonNull(allAudiogramPoints, "allAudiogramPoints cannot be null");
        Objects.requireNonNull(message, "message cannot be null");

        allAudiogramPoints = List.copyOf(allAudiogramPoints);
    }
}
