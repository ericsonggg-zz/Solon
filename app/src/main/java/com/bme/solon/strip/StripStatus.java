package com.bme.solon.strip;

public enum StripStatus {
    DRY(0),
    NO_UTI(1),
    LIGHT_UTI(2),
    MEDIUM_UTI(3),
    SEVERE_UTI(4);

    private int severity;

    private StripStatus(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }
}
