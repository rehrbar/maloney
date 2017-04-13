package ch.hsr.maloney.storage;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;

import java.io.*;
import java.util.*;

/**
 * Created by roman on 10.04.17.
 */
public class EventQueue {

    private final Logger logger;
    private HTreeMap.KeySet<Event> events;
    DB db;
    Map<Event, Set<JobExecution>> checkedOutEvents = new HashMap<>();

    public EventQueue(){
        this.logger = LogManager.getLogger();
        File file = null;
        try {
            file = File.createTempFile("maloney",".db");
            file.delete();
            // TODO use some management of db files
            db = DBMaker.fileDB(file).make();// TODO do we need memory mapped or custom serializer?
            events = db.hashSet("events", new MySerializer()).createOrOpen();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds the event of the job execution to the queue.
     */
    public void add(JobExecution jobExecution){
        Event event = jobExecution.getTrigger();
        events.add(event);
        // TODO improve speed of inserts
        Set<JobExecution> executions = checkedOutEvents.get(event);

        // Create list if it  does not exist.
        if(executions == null){
            executions = new LinkedHashSet<>();
            checkedOutEvents.put(event, executions);
        }
        executions.add(jobExecution);
    }

    /**
     * Removes the job execution from the persistent storage.
     */
    public void remove(JobExecution execution){
        Set<JobExecution> executions = checkedOutEvents.get(execution.getTrigger());
        if(executions == null){
            // TODO throw an error or put assertions here?
            logger.warn("Tried to remove a not queued job execution.");
            return;
        }

        executions.remove(execution);

        if(executions.isEmpty()){
            checkedOutEvents.remove(executions);
            events.remove(execution.getTrigger());
        }
    }

    /**
     * Closes the underlying db.
     */
    public void close(){
        db.close();
    }

    private class MySerializer implements Serializer<Event> {
        final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull Event value) throws IOException {
            mapper.writeValue((DataOutput)out, value);
        }

        @Override
        public Event deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return mapper.readValue(input, Event.class);
        }
    }
}
