package ch.hsr.maloney.storage;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by roman on 10.04.17.
 */
public class EventQueue {

    private HTreeMap.KeySet<Event> events;
    DB db;
    Map<JobExecution, Event> checkedOutEvents = new HashMap<>();

    public EventQueue(){
        File file = null;
        try {
            file = File.createTempFile("maloney",".db");
            file.delete();
            db = DBMaker.fileDB(file).make();// TODO do we need memory mapped or custom serializer?
            events = db.hashSet("events", new MySerializer()).createOrOpen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO add queue for running/checked out events

    }

    /**
     * Adds a new event to the store.
     * @param evt Event to add.
     */
    public void add(Event evt){
        events.add(evt);
    }

    /**
     * Removes the top most element which is not checked.
     * @return Top most element.
     */
    public JobExecution peek(Job job){
        Collection<Event> values = checkedOutEvents.values();
        for (Event event : events) {
            if(!values.contains(event)){
                JobExecution jobExecution = new JobExecution(job, event);
                checkedOutEvents.put(jobExecution, event);
                return jobExecution;
            }
        }
        return null; // TODO throw an error?
    }

    /**
     * Removes the element from the collection and deletes checkout flag.
     * @param execution execution to remove.
     */
    public void remove(JobExecution execution){
        Event evt = checkedOutEvents.remove(execution);
        events.remove(evt);
    }

    /**
     * Closes the underlying db.
     */
    public void close(){
        db.close();
    }

    private class MySerializer implements Serializer<Event> {
        // TODO fix java.lang.IllegalArgumentException: Key.hashCode() changed after serialization, make sure to use correct Key Serializer

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
