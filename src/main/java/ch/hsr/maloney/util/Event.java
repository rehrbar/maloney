package ch.hsr.maloney.util;

import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class Event {
    private String name;
    private String origin; //Job Name
    private UUID uuid;

    /**
     * Constructor.
     * @param name      Name of the event
     * @param origin    Origin of the event, i.e. name of the Job that created this event
     * @param uuid      Uuid of the file concerned
     */
    public Event(String name, String origin, UUID uuid) {
        this.name = name;
        this.origin = origin;
        this.uuid = uuid;
    }

    /**
     *
     * @return Uuid of the file processed
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     *
     * @return Job which generated this Event
     */
    public String getOrigin() {
        return origin;
    }

    /**
     *
     * @return Name of this event
     */
    public String getName() {
        return name;
    }
}
