package ch.hsr.maloney.util.categorization;

/**
 * Created by oliver on 30.05.17.
 */
public enum DefaultCategory {
    KNOWN_GOOD("Known Good"),
    KNOWN_BAD("Known Bad"),
    UNKNOWN("Unknown");

    private String name;

    DefaultCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
