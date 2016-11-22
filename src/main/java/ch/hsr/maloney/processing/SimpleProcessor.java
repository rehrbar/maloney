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
    private final Map<Job, Event> queue; //TODO replace with better structure
    private final Map<Job, List<Event>> jobListMap;
    private Context ctx;

    public SimpleProcessor(Context ctx) {
        logger = LogManager.getLogger();
        this.ctx = ctx;
        queue = new HashMap<>();
        jobListMap = new HashMap<>();
    }

    @Override
    public void start() {
        // TODO Review processing of events. This implementation will
        // not handle events, which are added later, well.
        List<Job> removeWhenDone = new LinkedList<>();
        while (!queue.isEmpty()) {
            queue.forEach((job, event) -> {
                if (job.canRun(ctx, event)) {
                    job.run(ctx, event); //TODO notify framework about new Events
                    removeWhenDone.add(job);
                }
            });
            removeWhenDone.forEach((queue::remove));
        }
    }

    public void otherStart() {
        List<Job> removeWhenDone = new LinkedList<>();
        while(!jobListMap.isEmpty()){
            jobListMap.forEach((job, eventList) -> {
                eventList.forEach((event -> {
                    if (job.canRun(ctx, event)){
                        List<Event> createdEvents = job.run(ctx, event); //TODO notify framework about new Events
                        notifyObservers(createdEvents);
                        removeWhenDone.add(job);
                    }
                }));

            });
            removeWhenDone.forEach((jobListMap::remove));
        }
    }

    @Override
    public void stop() {
        //TODO: Not necessary as of now, because it's sequential. But in the future maybe?
    }

    @Override
    public void enqueue(Job job, Event event) {
        queue.put(job, event);

        List<Event> eventList;
        if(jobListMap.get(job) != null){
            eventList = jobListMap.get(job);
        } else {
            eventList = new LinkedList<>();
        }
        jobListMap.put(job, eventList);
    }
}
