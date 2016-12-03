package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author oniet
 *
 * Should handle the running of jobs.
 *
 * The stuff inside of method start() somewhat resembles the Reactor Pattern
 * https://en.wikipedia.org/wiki/Reactor_pattern
 *
 * In the end, this should be a Proactor (asynchronous), but it's still very much work in progress.
 */

class Tuple<K,V>{
    final private K left;
    final private V right;

    Tuple(K left, V right) {
        this.left = left;
        this.right = right;
    }

    K getLeft() {
        return left;
    }

    V getRight() {
        return right;
    }

}

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
                setChanged();
                notifyObservers(createdEvents);
            } else {
                // Job could not be run and is put at the end of the queue
                jobQueue.add(tuple);
                //TODO remove job if it can't be completed ever
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
