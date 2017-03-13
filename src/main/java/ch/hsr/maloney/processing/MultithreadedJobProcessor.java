package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * @author oniet
 *
 * Handles the running of jobs.
 *
 * Uses Threadpool (ForkJoinPool) and Futures and to run Jobs and the Observer Pattern to notify the Framwork.
 *
 */
public class MultithreadedJobProcessor extends JobProcessor{
    private final Logger logger;
    private final Queue<Tuple<Job, Event>> jobQueue; //TODO replace with better Queueing structure (persistent)
    private Context ctx;

    private Thread controllerThread;
    private boolean stopProcessing;

    public MultithreadedJobProcessor(Context ctx){
        logger = LogManager.getLogger();
        this.ctx = ctx;
        jobQueue = new LinkedList<>();

        stopProcessing = false;
    }

    @Override
    public void start() {
        logger.debug("Starting JobProcessor with {} Event(s) queued", jobQueue.size());
        controllerThread = new Thread(()->{
            logger.debug("Started controllerThread");
            List<Future<List<Event>>> pendingEvents = new LinkedList<>();
            ForkJoinPool pool = new ForkJoinPool();

            while(!stopProcessing){
                if(!jobQueue.isEmpty()){
                    Tuple<Job, Event> tuple = jobQueue.poll();
                    Job job = tuple.getLeft();
                    Event evt = tuple.getRight();

                    if(job.canRun(ctx, evt)){
                        Future<List<Event>> createdEvents = pool.submit(()-> job.run(ctx, evt));
                        pendingEvents.add(createdEvents);
                    }
                }
                pushFinishedEventsUp(pendingEvents);
            }
            while(!pendingEvents.isEmpty()){
                pushFinishedEventsUp(pendingEvents);
            }
            logger.debug("Nothing more to process or processing canceled");
        });

        controllerThread.start();
    }

    /**
     * Check whether any Jobs have finished. If so, tell the framework and remove them from the list
     *
     * @param futureEventsList List of Future\<Event\> to check for completion
     */
    private void pushFinishedEventsUp(List<Future<List<Event>>> futureEventsList) {
        List<Future<List<Event>>> finishedEventLists = new LinkedList<>();

        for(Future<List<Event>> futureEvents : futureEventsList){
            if(futureEvents.isDone()){
                try {
                    logger.debug("Notifying Framework about completed Events");
                    setChanged();
                    notifyObservers(futureEvents.get());
                } catch (InterruptedException e) {
                    logger.error("Job was interrupted",e);
                } catch (ExecutionException e) {
                    logger.error("Job caught exception",e);
                }
                finishedEventLists.add(futureEvents);
            }
        }

        futureEventsList.removeAll(finishedEventLists);
    }

    @Override
    public void stop() {
        if(controllerThread.getState() != Thread.State.TERMINATED){
            logger.debug("Terminating JobProcessor...");
            stopProcessing = true;
            try {
                controllerThread.join();
            } catch (InterruptedException e) {
                logger.fatal("Could not stop JobProcessor");
            }
        } else {
            logger.debug("JobProcessor was already terminated");
        }
    }

    @Override
    public void enqueue(Job job, Event event) {
        logger.debug("Enqueued '{}' to '{}'", event.getName(), job.getJobName());
        jobQueue.add(new Tuple<>(job, event));
    }
}
