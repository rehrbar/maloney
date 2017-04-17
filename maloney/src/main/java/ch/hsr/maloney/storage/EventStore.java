package ch.hsr.maloney.storage;

import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;
import org.mapdb.serializer.GroupSerializerObjectArray;
import org.mapdb.serializer.SerializerJava;
import org.mapdb.serializer.SerializerUUID;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by roman on 10.04.17.
 */
public class EventStore {

    private final Logger logger;
    private BTreeMap<UUID, Event> events;
    DB db;
    Map<Event, Set<JobExecution>> checkedOutEvents = new HashMap<>();

    public EventStore() {
        this(true);
    }

    public EventStore(boolean persistent) {
        this.logger = LogManager.getLogger();
        if(persistent) {
            File file = Paths.get(System.getProperty("java.io.tmpdir"), "maloney-events.db").toFile();
            // TODO use memoryDB for some unittests/allow configuration if persistent or not
            db = DBMaker.fileDB(file)
                    .fileMmapEnableIfSupported()
                    .transactionEnable()
                    .allocateStartSize(1 * 1024 * 1024 * 1024)  // 1GB
                    .allocateIncrement(512 * 1024 * 1024)       // 512MB
                    .closeOnJvmShutdown()
                    .make();
        } else {
            db = DBMaker.memoryDB().closeOnJvmShutdown().make();
        }
        // TODO use different set names for different cases
        events = db.treeMap("events", new SerializerUUID(), new MySerializer()).createOrOpen();
    }

    /**
     * Adds the event of the job execution to the queue.
     */
    public void add(JobExecution jobExecution) {
        // TODO add support for bulk imports
        Event event = jobExecution.getTrigger();

        // Only update if really necessary
        events.putIfAbsent(event.getId(), event);
        // TODO improve speed of inserts
        Set<JobExecution> executions = checkedOutEvents.get(event);

        // Create list if it  does not exist.
        if (executions == null) {
            executions = new LinkedHashSet<>();
            checkedOutEvents.put(event, executions);
        }
        db.commit();
        executions.add(jobExecution);
    }

    /**
     * Removes the job execution from the persistent storage.
     */
    public void remove(JobExecution execution) {
        Set<JobExecution> executions = checkedOutEvents.get(execution.getTrigger());
        if (executions == null) {
            // TODO throw an error or put assertions here?
            logger.warn("Tried to remove a not queued job execution.");
            return;
        }

        executions.remove(execution);

        if (executions.isEmpty()) {
            checkedOutEvents.remove(executions);
            events.remove(execution.getTrigger().getId());
        }
        db.commit();
    }

    /**
     * Gets all stored events.
     * @return All stored events.
     */
    public Collection<Event> getEvents(){
        return events.getValues();
    }

    /**
     * Checks if there are stored events.
     * @return True if the store has events.
     */
    public boolean hasEvents(){
        return !events.isEmpty();
    }

    /**
     * Closes the underlying db.
     */
    public void close() {
        db.close();
        // TODO delete db after successful run
    }

    private class MySerializer extends GroupSerializerObjectArray<Event> implements Serializer<Event> {
        final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull Event value) throws IOException {
            mapper.writeValue((DataOutput) out, value);
        }

        @Override
        public Event deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return mapper.readValue(input, Event.class);
        }
    }
}
