package com.odyometre.software;

import java.util.List;

public final class AudiometryConfig {
    private AudiometryConfig() {}

    public static final List<Integer> FREQUENCIES_HZ = List.of(250, 500, 1000, 2000, 4000, 8000);
    public static final List<Ear> EARS = List.of(Ear.RIGHT, Ear.LEFT);

    public static final int START_DB = 40;
    public static final int MIN_DB = 0;
    public static final int MAX_DB = 100;
    public static final int STEP_DOWN_DB = 10;
    public static final int STEP_UP_DB = 5;
    public static final int DURATION_MS = 2000;
    public static final int RESPONSE_TIMEOUT_MS = 3000;
}
