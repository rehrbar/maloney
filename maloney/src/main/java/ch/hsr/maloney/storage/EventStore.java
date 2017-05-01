package ch.hsr.maloney.storage;

import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;
import org.mapdb.serializer.GroupSerializerObjectArray;
import org.mapdb.serializer.SerializerUUID;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by roman on 10.04.17.
 */
public class EventStore {
    private static final int COMMIT_INTERVAL = 5;
    private static final TimeUnit COMMIT_INTERVAL_UNIT = TimeUnit.SECONDS;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Logger logger;
    private DB db;
    private Map<UUID, Set<UUID>> checkedOutEvents = new HashMap<>();
    private ScheduledFuture deferredCommit;
    private BTreeMap<UUID, Event> events;

    public EventStore() {
        this(true);
    }

    public EventStore(boolean persistent) {
        this.logger = LogManager.getLogger();
        if (persistent) {
            File file = Paths.get(System.getProperty("java.io.tmpdir"), "maloney-events.db").toFile();
            // TODO use memoryDB for some unittests/allow configuration if persistent or not
            db = DBMaker.fileDB(file)
                    .fileMmapEnableIfSupported()
                    .transactionEnable()
                    .allocateStartSize(128 * 1024 * 1024) // 128MB
                    .allocateIncrement(64 * 1024 * 1024)  // 64MB
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
        add(new LinkedList<JobExecution>() {{
            push(jobExecution);
        }});
    }

    /**
     * Adds events of the job executions to the queue.
     */
    public synchronized void add(Collection<JobExecution> jobExecutions) {
        for (JobExecution jobExecution : jobExecutions) {
            Event event = jobExecution.getTrigger();

            // Create and insert list if it  does not exist.
            Set<UUID> executions = checkedOutEvents.computeIfAbsent(event.getId(), k -> new LinkedHashSet<>());

            executions.add(jobExecution.getId());
        }
        // If duplicated events are found, inserting only the first occurrence and dismiss others.
        events.putAll(jobExecutions.stream().collect(Collectors.toMap(j -> j.getTrigger().getId(), j -> j.getTrigger(), (v1, v2) -> v1)));
        scheduleCommitIfNecessary();
    }

    private synchronized void commit() {
        db.commit();
    }

    /**
     * Removes the job execution from the persistent storage.
     */
    public synchronized void remove(JobExecution execution) {
        Set<UUID> executions = checkedOutEvents.get(execution.getTrigger().getId());
        if (executions == null) {
            // TODO throw an error or put assertions here?
            logger.warn("Tried to remove a not queued job execution.");
            return;
        }

        executions.remove(execution.getId());

        if (executions.isEmpty()) {
            checkedOutEvents.remove(execution.getTrigger().getId());
            events.remove(execution.getTrigger().getId());
        }
        scheduleCommitIfNecessary();
    }

    public synchronized void clear(){
        checkedOutEvents.clear();
        events.clear();
        commit();
    }

    private void scheduleCommitIfNecessary() {
        if (deferredCommit == null || deferredCommit.isDone()) {
            deferredCommit = scheduler.schedule(this::commit, COMMIT_INTERVAL, COMMIT_INTERVAL_UNIT);
        }
    }

    /**
     * Gets all stored events.
     *
     * @return All stored events.
     */
    public synchronized Collection<Event> getEvents() {
        return events.getValues();
    }

    /**
     * Checks if there are stored events.
     *
     * @return True if the store has events.
     */
    public synchronized boolean hasEvents() {
        return !events.isEmpty();
    }

    /**
     * Closes the underlying db.
     */
    public synchronized void close() {
        deferredCommit.cancel(false);
        scheduler.shutdown();

        // Commit to clean up all write ahead logs.
        if(!db.isClosed()) {
            commit();
            db.close();
        }
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
