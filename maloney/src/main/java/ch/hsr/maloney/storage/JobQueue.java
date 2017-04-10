package ch.hsr.maloney.storage;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Tuple;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.tape2.ObjectQueue;
import com.squareup.tape2.QueueFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Created by roman on 10.04.17.
 */
public class JobQueue {

    private final QueueFile queueFile;
    private final ObjectQueue<JobExecution> jobExecutions;

    public JobQueue(){
        File file = null;
        try {
            file = Files.createTempFile("maloney",".db").toFile();
            queueFile = new QueueFile.Builder(file).build();
            jobExecutions = ObjectQueue.create(queueFile, new JobQueueConverter());
        } catch (IOException e) {
            // TODO replace with throws?
            e.printStackTrace();
        }
    }

    public class JobQueueConverter implements ObjectQueue.Converter<JobExecution> {
        final ObjectMapper mapper;

        public JobQueueConverter(){
            mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        }

        @Override
        public JobExecution from(byte[] bytes) throws IOException {
            return mapper.readValue(bytes, JobExecution.class);
        }

        @Override
        public void toStream(JobExecution o, OutputStream outputStream) throws IOException {
            mapper.writeValue(outputStream, o);
        }
    }

    class JobExecution extends Tuple<Job, Event> {
        JobExecution(Job job, Event event) {
            super(job, event);
        }

        public Job getJob() {
            return getLeft();
        }

        public Event getEvent() {
            return getRight();
        }
    }
}
