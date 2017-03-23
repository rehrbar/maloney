package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// TODO phrasing!
public class SimpleProcessor extends JobProcessor {
    private final Logger logger;
    private final Queue<Tuple<Job, Event>> jobQueue; //TODO replace with better Queueing structure (persistent)
    private Context ctx;

    public SimpleProcessor(Context ctx) {
        logger = LogManager.getLogger();
        this.ctx = ctx;
        jobQueue = new LinkedList<>();
    }

    @Override
    public void start() {
        logger.debug("Starting Processing");
        //TODO add the stuff below into a thread(pool)
        while(!jobQueue.isEmpty()){
            // while there are still jobs to be run...
            Tuple<Job, Event> tuple = jobQueue.poll();
            Job job = tuple.getLeft();
            Event evt = tuple.getRight();

            if(job.canRun(ctx, evt)){
                List<Event> createdEvents = job.run(ctx, evt);
                if(createdEvents != null && !createdEvents.isEmpty()){
                    setChanged();
                    notifyObservers(createdEvents);
                }
            }
        }
        logger.debug("Nothing more to process");
    }

    @Override
    public void stop() {
        //TODO: Not necessary as of now, because it's sequential (single Process). But in the future maybe?
    }

    @Override
    public void enqueue(Job job, Event event) {
        logger.debug("Job '{}' enqueued with new event '{}'", job.getJobName(), event.getName());
        jobQueue.add(new Tuple<>(job, event));
    }
}
