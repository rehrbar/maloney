package ch.hsr.maloney.util;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by olive_000 on 01.11.2016.
 */
public class Event{
    private UUID id;
    private String name;
    private String origin; //Job Name
    private UUID fileUuid;

    private Event(){
        // Keep for serialization
    }

    /**
     * Constructor.
     *
     * @param name     Name of the event
     * @param origin   Origin of the event, i.e. name of the Job that created this event
     * @param fileUuid Uuid of the file concerned
     */
    public Event(String name, String origin, UUID fileUuid) {
        id = UUID.randomUUID();
        this.name = name;
        this.origin = origin;
        this.fileUuid = fileUuid;
    }

    /**
     * @return Uuid of the file processed
     */
    public UUID getFileUuid() {
        return fileUuid;
    }

    /**
     * @return Job which generated this Event
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @return Name of this event
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, origin, fileUuid);
    }

    /**
     * @return Identifier of this event.
     */
    public UUID getId() {
        return id;
    }
}
