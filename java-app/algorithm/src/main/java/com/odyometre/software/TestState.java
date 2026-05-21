package com.odyometre.software;

import java.util.List;
import java.util.Objects;

public record TestState(
        Ear currentEar,
        int currentFrequencyHz,
        int currentDb,
        List<PatientResponse> responses,
        List<AudiogramPoint> audiogramPoints,
        boolean completed
) {
    public TestState {
        Objects.requireNonNull(currentEar, "currentEar cannot be null");
        Objects.requireNonNull(responses, "responses cannot be null");
        Objects.requireNonNull(audiogramPoints, "audiogramPoints cannot be null");

        responses = List.copyOf(responses);
        audiogramPoints = List.copyOf(audiogramPoints);
    }
}
