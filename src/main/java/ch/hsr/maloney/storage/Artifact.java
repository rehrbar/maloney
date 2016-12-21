package ch.hsr.maloney.storage;

/**
 * Created by olive_000 on 07.11.2016.
 */
public class Artifact {
    private String originator;
    private Object value;
    private String type;

    public Artifact() {
        // Required for deserializing.
    }

    /**
     * Creates a new Artifact
     *
     * @param originator Job which created this Artifact
     * @param value      Value of the Artifact
     * @param type       Type of the associated Value
     */
    public Artifact(String originator, Object value, String type) {
        this.originator = originator;
        this.value = value;
        this.type = type;
    }

    /**
     * @return Name of Job which created this Artifact
     */
    public String getOriginator() {
        return originator;
    }

    /**
     * @return Value of the Artifact
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return Type of the Value associated with the Artifact
     */
    public String getType() {
        return type;
    }
}
