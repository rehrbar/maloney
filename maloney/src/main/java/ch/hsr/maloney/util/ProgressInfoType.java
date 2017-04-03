package ch.hsr.maloney.util;

/**
 * Created by oliver on 28.03.17.
 */
public enum ProgressInfoType {
    FILE("Found Files"),
    NEWEVENTS("Created Events"),
    PROCESSEDEVENT("Processed Events");

    private final String fieldDescription;

    private ProgressInfoType(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
}
