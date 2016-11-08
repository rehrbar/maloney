package ch.hsr.maloney.storage;

/**
 * Created by olive_000 on 07.11.2016.
 */
public class Artifact {
    private String originator;
    private Object value;
    private String type;

    public Artifact(String originator, Object value, String type) {
        this.originator = originator;
        this.value = value;
        this.type = type;
    }

    public String getOriginator() {
        return originator;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
