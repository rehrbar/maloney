package ch.hsr.maloney.util;

/**
 * Created by oliver on 28.03.17.
 */
public enum ProgressInfoType {
    FILE("Found Files"),
    KNOWN_GOOD_FILE("Known Good Files"),
    KNOWN_BAD_FILE("Known Bad Files"),
    UNKNOWN_FILE("Unknown Files"),
    NEW_EVENTS("Created Events"),
    PROCESSED_EVENT("Processed Events");

    private final String fieldDescription;

    private ProgressInfoType(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
}
