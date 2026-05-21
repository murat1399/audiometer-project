package com.odyometre.software;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AudiometryAlgorithm {
    private AudiometryAlgorithm() {}

    public static TestState createInitialState() {
        return new TestState(
                AudiometryConfig.EARS.get(0),
                AudiometryConfig.FREQUENCIES_HZ.get(0),
                AudiometryConfig.START_DB,
                List.of(),
                List.of(),
                false
        );
    }

    public static int calculateNextDb(int currentDb, boolean heard) {
        return heard
                ? currentDb - AudiometryConfig.STEP_DOWN_DB
                : currentDb + AudiometryConfig.STEP_UP_DB;
    }

    public static AlgorithmStep processResponse(TestState state, PatientResponse response) {
        if (state.completed()) {
            return error(state, "Test zaten tamamlanmış durumda.");
        }
        if (!AudiometryValidator.isValidFrequency(response.frequencyHz())) {
            return error(state, "Geçersiz frekans değeri: " + response.frequencyHz() + " Hz.");
        }
        if (!AudiometryValidator.isValidDb(response.db())) {
            return error(state, "Geçersiz dB değeri: " + response.db() + " dB.");
        }
        if (!AudiometryValidator.responseMatchesState(state, response)) {
            return error(state, "Hasta cevabı mevcut test durumu ile uyuşmuyor.");
        }

        List<PatientResponse> updatedResponses = append(state.responses(), response);
        Optional<AudiogramPoint> threshold = detectThreshold(updatedResponses, state.currentEar(), state.currentFrequencyHz());

        if (threshold.isPresent()) {
            return handleThresholdFound(state, updatedResponses, threshold.get());
        }

        int nextDb = calculateNextDb(state.currentDb(), response.heard());
        if (!AudiometryValidator.isValidDb(nextDb)) {
            return error(
                    new TestState(state.currentEar(), state.currentFrequencyHz(), state.currentDb(), updatedResponses, state.audiogramPoints(), false),
                    "Bir sonraki dB değeri güvenli sınırların dışında: " + nextDb + " dB."
            );
        }

        TestState newState = new TestState(
                state.currentEar(),
                state.currentFrequencyHz(),
                nextDb,
                updatedResponses,
                state.audiogramPoints(),
                false
        );

        String message = response.heard()
                ? "Hasta duydu, 10 dB azaltıldı."
                : "Hasta duymadı, 5 dB artırıldı.";

        AlgorithmResult result = new AlgorithmResult(
                StepStatus.CONTINUE,
                Optional.of(state.currentEar()),
                Optional.of(state.currentFrequencyHz()),
                Optional.of(nextDb),
                Optional.empty(),
                state.audiogramPoints(),
                message
        );

        return new AlgorithmStep(newState, result);
    }

    public static Optional<AudiogramPoint> detectThreshold(List<PatientResponse> responses, Ear ear, int frequencyHz) {
        List<PatientResponse> sameFrequencyResponses = responses.stream()
                .filter(response -> response.ear() == ear)
                .filter(response -> response.frequencyHz() == frequencyHz)
                .toList();

        if (sameFrequencyResponses.size() < 2) {
            return Optional.empty();
        }

        PatientResponse previous = sameFrequencyResponses.get(sameFrequencyResponses.size() - 2);
        PatientResponse last = sameFrequencyResponses.get(sameFrequencyResponses.size() - 1);

        if (!previous.heard() && last.heard()) {
            return Optional.of(new AudiogramPoint(ear, frequencyHz, last.db()));
        }

        return Optional.empty();
    }

    private static AlgorithmStep handleThresholdFound(TestState state, List<PatientResponse> updatedResponses, AudiogramPoint point) {
        List<AudiogramPoint> updatedPoints = append(state.audiogramPoints(), point);
        Optional<NextPosition> next = findNextPosition(state.currentEar(), state.currentFrequencyHz());

        if (next.isEmpty()) {
            TestState completedState = new TestState(
                    state.currentEar(),
                    state.currentFrequencyHz(),
                    state.currentDb(),
                    updatedResponses,
                    updatedPoints,
                    true
            );

            AlgorithmResult completedResult = new AlgorithmResult(
                    StepStatus.TEST_COMPLETED,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    updatedPoints,
                    "Test tamamlandı."
            );

            return new AlgorithmStep(completedState, completedResult);
        }

        NextPosition nextPosition = next.get();
        TestState newState = new TestState(
                nextPosition.ear(),
                nextPosition.frequencyHz(),
                AudiometryConfig.START_DB,
                updatedResponses,
                updatedPoints,
                false
        );

        AlgorithmResult result = new AlgorithmResult(
                StepStatus.THRESHOLD_FOUND,
                Optional.of(nextPosition.ear()),
                Optional.of(nextPosition.frequencyHz()),
                Optional.of(AudiometryConfig.START_DB),
                Optional.of(point),
                updatedPoints,
                point.frequencyHz() + " Hz için eşik bulundu. Bir sonraki frekansa geçiliyor."
        );

        return new AlgorithmStep(newState, result);
    }

    private static Optional<NextPosition> findNextPosition(Ear currentEar, int currentFrequencyHz) {
        int frequencyIndex = AudiometryConfig.FREQUENCIES_HZ.indexOf(currentFrequencyHz);
        int earIndex = AudiometryConfig.EARS.indexOf(currentEar);

        if (frequencyIndex < AudiometryConfig.FREQUENCIES_HZ.size() - 1) {
            return Optional.of(new NextPosition(currentEar, AudiometryConfig.FREQUENCIES_HZ.get(frequencyIndex + 1)));
        }

        if (earIndex < AudiometryConfig.EARS.size() - 1) {
            return Optional.of(new NextPosition(AudiometryConfig.EARS.get(earIndex + 1), AudiometryConfig.FREQUENCIES_HZ.get(0)));
        }

        return Optional.empty();
    }

    private static AlgorithmStep error(TestState state, String message) {
        AlgorithmResult result = new AlgorithmResult(
                StepStatus.ERROR,
                Optional.ofNullable(state.currentEar()),
                Optional.of(state.currentFrequencyHz()),
                Optional.empty(),
                Optional.empty(),
                state.audiogramPoints(),
                message
        );
        return new AlgorithmStep(state, result);
    }

    private static <T> List<T> append(List<T> source, T item) {
        List<T> copy = new ArrayList<>(source);
        copy.add(item);
        return List.copyOf(copy);
    }

    private record NextPosition(Ear ear, int frequencyHz) {}
}
