package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by olive_000 on 08.11.2016.
 */
public class SimpleProcessor extends JobProcessor {
    private Map<Job, Event> queue; //TODO replace Map with Dictionary
    private Context ctx;

    public SimpleProcessor(Context ctx) {
        this.ctx = ctx;
        queue = new HashMap<>();
    }

    @Override
    public void start() {
        // TODO Review processing of events. This implementation will
        // not handle events, which are added later, well.
        List<Job> removeWhenDone = new LinkedList<>();
        while(!queue.isEmpty()){
            queue.forEach((job, event) -> {
                if (job.canRun(ctx, event)){
                    job.run(ctx, event); //TODO notify framework about new Events
                    removeWhenDone.add(job);
                }
            });
            removeWhenDone.forEach((job -> queue.remove(job)));
        }
    }

    @Override
    public void stop() {
        //TODO: Not necessary as of now, because it's sequential. But in the future maybe?
    }

    @Override
    public void enqueue(Job job, Event event) {
        queue.put(job, event);
    }
}
