package ch.hsr.maloney.core;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class Event {
    private String name;
    private String origin; //Job Name
    private String uuid;

    public Event(String name, String origin, String uuid) {
        this.name = name;
        this.origin = origin;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getOrigin() {
        return origin;
    }

    public String getName() {
        return name;
    }
}
