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
 * Created by olive_000 on 08.11.2016.
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
                        notifyObservers(createdEvents);
                    }
                }
                // Now, remove processed events from eventList ...
                processedEvents.forEach(eventList::remove);
                processedEvents.clear();
                // ... and if there are no more events for this job, mark the job for removal ...
                if(eventList.size() == 0){
                    removeWhenDone.add(job);
                }
            });
            // ... finally, remove jobs where there are no more events pending ...
            removeWhenDone.forEach(jobQueue::remove);
            removeWhenDone.clear();
        }
    }

    @Override
    public void stop() {
        //TODO: Not necessary as of now, because it's sequential (single Process). But in the future maybe?
    }

    @Override
    public void enqueue(Job job, Event event) {
        List<Event> eventList = jobQueue.get(job);
        if(eventList == null){
            eventList = new LinkedList<>();
        }
        eventList.add(event);
        jobQueue.put(job, eventList);
    }
}
