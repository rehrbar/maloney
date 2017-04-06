package ch.hsr.maloney.util;

/**
 * Created by oliver on 28.03.17.
 */
public enum ProgressInfoType {
    NEW_EVENT("Created Events"),
    PROCESSED_EVENT("Processed Events");

    private final String name;

    ProgressInfoType(String value) {
        name = value;
    }

    public String toString(){
        return this.name;
    }
}
