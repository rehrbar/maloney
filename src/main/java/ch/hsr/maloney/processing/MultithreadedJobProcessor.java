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
 * Uses Threadpool (ForkJoinPool) and Futures and to run Jobs and the Observer Pattern notify Framwork.
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
        logger.debug("Starting JobProcessor...");
        controllerThread = new Thread(()->{
            List<Future<List<Event>>> pendingEvents = new LinkedList<>();
            ForkJoinPool pool = new ForkJoinPool();


            while(!jobQueue.isEmpty() && !stopProcessing){
                // while there are still jobs to be run...
                Tuple<Job, Event> tuple = jobQueue.poll();
                Job job = tuple.getLeft();
                Event evt = tuple.getRight();

                if(job.canRun(ctx, evt)){
                    Future<List<Event>> createdEvents = pool.submit(()-> job.run(ctx, evt));
                    pendingEvents.add(createdEvents);
                }

                // Check whether any Jobs have finished. If so tell the framework and remove them from the list
                List<Future<List<Event>>> finishedEventLists = new LinkedList<>();

                for(Future<List<Event>> futureEventList : pendingEvents){
                    if(futureEventList.isDone()){
                        try {
                            notifyObservers(futureEventList.get());
                            setChanged();
                        } catch (InterruptedException e) {
                            logger.error("Job was interrupted",e);
                        } catch (ExecutionException e) {
                            logger.error("Job caught exception",e);
                        }
                        finishedEventLists.add(futureEventList);
                    }
                }

                pendingEvents.removeAll(finishedEventLists);
            }
            logger.debug("Nothing more to process or processing canceled");
        });

        controllerThread.start();
    }

    @Override
    public void stop() {
        logger.debug("Stopping JobProcessor...");
        stopProcessing = true;
        try {
            controllerThread.join();
        } catch (InterruptedException e) {
            logger.fatal("Could not stop JobProcessor");
        }
    }

    @Override
    public void enqueue(Job job, Event event) {
        logger.debug("Enqueued '{}' to '{}'", event.getName(), " to " + job.getJobName());
        jobQueue.add(new Tuple<>(job, event));
    }
}
