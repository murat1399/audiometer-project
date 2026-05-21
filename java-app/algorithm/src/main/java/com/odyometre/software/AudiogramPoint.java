package com.odyometre.software;

import java.util.Objects;

public record AudiogramPoint(
        Ear ear,
        int frequencyHz,
        int thresholdDb
) {
    public AudiogramPoint {
        Objects.requireNonNull(ear, "ear cannot be null");
    }
}
