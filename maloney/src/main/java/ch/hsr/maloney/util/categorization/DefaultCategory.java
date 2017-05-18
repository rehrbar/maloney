package ch.hsr.maloney.util.categorization;

/**
 * Created by oliver on 18.05.17.
 */
public enum DefaultCategory {
    KNOWN_BAD("Known Bad"),
    KNOWN_GOOD("Known Good"),
    UNKNOWN("Unknown");

    private final String name;

    DefaultCategory(String value) {
        name = value;
    }

    public String toString(){
        return this.name;
    }
}
