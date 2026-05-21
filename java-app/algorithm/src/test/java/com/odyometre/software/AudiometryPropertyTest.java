package com.odyometre.software;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AudiometryPropertyTest {

    @Property
    void heardResponse_shouldAlwaysDecreaseDbByTen(@ForAll @IntRange(min = 10, max = 100) int db) {
        assertEquals(db - 10, AudiometryAlgorithm.calculateNextDb(db, true));
    }

    @Property
    void notHeardResponse_shouldAlwaysIncreaseDbByFive(@ForAll @IntRange(min = 0, max = 95) int db) {
        assertEquals(db + 5, AudiometryAlgorithm.calculateNextDb(db, false));
    }

    @Property
    void parser_shouldNeverThrowException(@ForAll String rawMessage) {
        assertDoesNotThrow(() -> AudiometryParser.parseResponseMessage(rawMessage));
    }

    @Property
    void validDb_shouldBeBetweenZeroAndHundred(@ForAll int db) {
        boolean valid = AudiometryValidator.isValidDb(db);
        assertEquals(db >= 0 && db <= 100, valid);
    }

    @Provide
    Arbitrary<Integer> supportedFrequencies() {
        return Arbitraries.of(250, 500, 1000, 2000, 4000, 8000);
    }

    @Property
    void parser_shouldAcceptOnlySupportedFrequencies(@ForAll("supportedFrequencies") int frequency) {
        String message = "RESPONSE;EAR=RIGHT;FREQ=" + frequency + ";DB=40";
        Optional<PatientResponse> parsed = AudiometryParser.parseResponseMessage(message);
        assertTrue(parsed.isPresent());
    }
}
