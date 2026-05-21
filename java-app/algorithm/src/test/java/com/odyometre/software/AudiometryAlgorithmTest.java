package com.odyometre.software;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AudiometryAlgorithmTest {

    @Test
    void calculateNextDb_shouldDecreaseTenDb_whenPatientHeard() {
        assertEquals(30, AudiometryAlgorithm.calculateNextDb(40, true));
    }

    @Test
    void calculateNextDb_shouldIncreaseFiveDb_whenPatientDidNotHear() {
        assertEquals(45, AudiometryAlgorithm.calculateNextDb(40, false));
    }

    @Test
    void parser_shouldParseDetailedResponseMessage() {
        Optional<PatientResponse> parsed = AudiometryParser.parseResponseMessage(
                "RESPONSE;EAR=RIGHT;FREQ=1000;DB=40"
        );

        assertTrue(parsed.isPresent());
        assertEquals(new PatientResponse(Ear.RIGHT, 1000, 40, true), parsed.get());
    }

    @Test
    void parser_shouldReturnEmpty_whenMessageIsInvalid() {
        assertTrue(AudiometryParser.parseResponseMessage("RESPONS").isEmpty());
        assertTrue(AudiometryParser.parseResponseMessage(null).isEmpty());
        assertTrue(AudiometryParser.parseResponseMessage("RESPONSE;EAR=RIGHT;FREQ=9000;DB=40").isEmpty());
    }

    @Test
    void processResponse_shouldContinueWithThirtyDb_whenPatientHeardAtFortyDb() {
        TestState state = new TestState(Ear.RIGHT, 1000, 40, List.of(), List.of(), false);
        PatientResponse response = new PatientResponse(Ear.RIGHT, 1000, 40, true);

        AlgorithmStep step = AudiometryAlgorithm.processResponse(state, response);

        assertEquals(StepStatus.CONTINUE, step.result().status());
        assertEquals(30, step.newState().currentDb());
        assertEquals(1, step.newState().responses().size());
        assertEquals(0, state.responses().size(), "Original state must stay unchanged");
    }

    @Test
    void processResponse_shouldFindThreshold_afterNoResponseThenHeardResponse() {
        TestState state1 = new TestState(Ear.RIGHT, 1000, 40, List.of(), List.of(), false);
        AlgorithmStep step1 = AudiometryAlgorithm.processResponse(state1, new PatientResponse(Ear.RIGHT, 1000, 40, true));

        AlgorithmStep step2 = AudiometryAlgorithm.processResponse(step1.newState(), new PatientResponse(Ear.RIGHT, 1000, 30, false));
        AlgorithmStep step3 = AudiometryAlgorithm.processResponse(step2.newState(), new PatientResponse(Ear.RIGHT, 1000, 35, true));

        assertEquals(StepStatus.THRESHOLD_FOUND, step3.result().status());
        assertTrue(step3.result().audiogramPoint().isPresent());
        assertEquals(new AudiogramPoint(Ear.RIGHT, 1000, 35), step3.result().audiogramPoint().get());
    }

    @Test
    void processResponse_shouldReturnError_whenResponseDoesNotMatchState() {
        TestState state = new TestState(Ear.RIGHT, 1000, 40, List.of(), List.of(), false);
        PatientResponse wrongResponse = new PatientResponse(Ear.LEFT, 1000, 40, true);

        AlgorithmStep step = AudiometryAlgorithm.processResponse(state, wrongResponse);

        assertEquals(StepStatus.ERROR, step.result().status());
    }
}
