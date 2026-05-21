package com.odyometre.software;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AudiometryParser {
    private AudiometryParser() {}

    public static Optional<PatientResponse> parseResponseMessage(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        String[] parts = message.trim().split(";");
        if (parts.length == 0 || !"RESPONSE".equals(parts[0])) {
            return Optional.empty();
        }

        try {
            Map<String, String> fields = Arrays.stream(parts)
                    .skip(1)
                    .map(String::trim)
                    .filter(part -> part.contains("="))
                    .map(part -> part.split("=", 2))
                    .filter(pair -> pair.length == 2)
                    .collect(Collectors.toMap(pair -> pair[0].trim(), pair -> pair[1].trim(), (a, b) -> b));

            Ear ear = Ear.valueOf(fields.get("EAR"));
            int frequency = Integer.parseInt(fields.get("FREQ"));
            int db = Integer.parseInt(fields.get("DB"));

            if (!AudiometryValidator.isValidFrequency(frequency) || !AudiometryValidator.isValidDb(db)) {
                return Optional.empty();
            }

            return Optional.of(new PatientResponse(ear, frequency, db, true));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public static boolean containsAnyValidResponse(String rawBatch) {
        if (rawBatch == null || rawBatch.isBlank()) {
            return false;
        }

        return Arrays.stream(rawBatch.split("\\R"))
                .filter(line -> line != null && !line.isBlank())
                .map(AudiometryParser::parseResponseMessage)
                .filter(Optional::isPresent)
                .map(optionalResponse -> true)
                .reduce(false, (previous, current) -> previous || current);
    }
}
