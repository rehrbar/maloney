package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class SimpleProcessor extends JobProcessor {
    private final Logger logger;
    private final Map<Job, List<Event>> jobQueue; //TODO replace with better Queueing structure
    private Context ctx;

    public SimpleProcessor(Context ctx) {
        logger = LogManager.getLogger();
        this.ctx = ctx;
        jobQueue = new HashMap<>();
    }

    @Override
    public void start() {
        logger.debug("Starting Processing");
        List<Job> removeWhenDone = new LinkedList<>();
        while(!jobQueue.isEmpty()){
            // while there are still jobs to be run..
            jobQueue.forEach((job, eventList) -> {
                // ... go through their queued events ...
                LinkedList<Event> processedEvents = new LinkedList<>();
                for(Event evt : eventList){
                    // ... if possible run the job with the event ...
                    if (job.canRun(ctx, evt)){
                        List<Event> createdEvents = job.run(ctx, evt);
                        // ... and mark the processed event for removal
                        processedEvents.add(evt);
                        setChanged();
                        notifyObservers(createdEvents);
                    }
                }
                // Now, remove processed events from this Jobs' eventList ...
                processedEvents.forEach(eventList::remove);
                processedEvents.clear();
                // ... and if there are no more events for this job, mark the job for removal ...
                if(eventList.size() == 0){
                    logger.debug("No more events for Job '{}', will be removed from processing,");
                    removeWhenDone.add(job);
                }
            });
            // ... finally, remove that Job
            removeWhenDone.forEach(jobQueue::remove);
            removeWhenDone.clear();
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
        List<Event> eventList = jobQueue.get(job);
        if(eventList == null){
            eventList = new LinkedList<>();
        }
        eventList.add(event);
        jobQueue.put(job, eventList);
    }
}
