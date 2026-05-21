package com.odyometre.software;

import java.util.Objects;

public record PatientResponse(
        Ear ear,
        int frequencyHz,
        int db,
        boolean heard
) {
    public PatientResponse {
        Objects.requireNonNull(ear, "ear cannot be null");
    }
}
