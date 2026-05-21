package com.odyometre.software;

public final class AudiometryValidator {
    private AudiometryValidator() {}

    public static boolean isValidFrequency(int frequencyHz) {
        return AudiometryConfig.FREQUENCIES_HZ.contains(frequencyHz);
    }

    public static boolean isValidDb(int db) {
        return db >= AudiometryConfig.MIN_DB && db <= AudiometryConfig.MAX_DB;
    }

    public static boolean responseMatchesState(TestState state, PatientResponse response) {
        return state.currentEar() == response.ear()
                && state.currentFrequencyHz() == response.frequencyHz()
                && state.currentDb() == response.db();
    }
}
