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
import java.util.*;

/**
 * Created by roman on 10.04.17.
 */
public class EventStore {

    private final Logger logger;
    private BTreeMap<UUID, Event> events;
    DB db;
    Map<Event, Set<JobExecution>> checkedOutEvents = new HashMap<>();
    Map<Event, UUID> eventKeys = new HashMap<>();

    public EventStore() {
        this.logger = LogManager.getLogger();
        File file = null;
        try {
            file = File.createTempFile("maloney", ".db");
            file.delete();
            // TODO use some management of db files
            db = DBMaker.fileDB(file)
                    .fileMmapEnableIfSupported()
                    .transactionEnable()
                    .allocateStartSize(1 * 1024 * 1024 * 1024)  // 1GB
                    .allocateIncrement(512 * 1024 * 1024)       // 512MB
                    .closeOnJvmShutdown()
                    .make();
            events = db.treeMap("events", new SerializerUUID(), new MySerializer()).createOrOpen();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds the event of the job execution to the queue.
     */
    public void add(JobExecution jobExecution) {
        Event event = jobExecution.getTrigger();
        UUID id = UUID.randomUUID();
        eventKeys.put(event, id);
        events.put(id, event);
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
            //events.remove(execution.getTrigger());
            events.remove(eventKeys.get(execution.getTrigger()));
        }
        db.commit();
    }

    /**
     * Closes the underlying db.
     */
    public void close() {
        db.close();
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
