package ch.hsr.maloney.util;

/**
 * Created by oliver on 28.03.17.
 */
public enum ProgressInfoType {
    TASK_QUEUED("Created Tasks"),
    TASK_FINISHED("Finished Tasks");

    private final String name;

    ProgressInfoType(String value) {
        name = value;
    }

    public String toString(){
        return this.name;
    }
}
