package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

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
    private final Queue<List<Event>> finishedQueue; //TODO replace with better Queueing structure (persistent)

    private Context ctx;

    private Thread controllerThread;
    private boolean stopProcessing;

    public MultithreadedJobProcessor(Context ctx){
        logger = LogManager.getLogger();
        this.ctx = ctx;
        jobQueue = new LinkedList<>();
        finishedQueue = new LinkedList<>();

        stopProcessing = false;
    }

    @Override
    public void start() {
        logger.debug("Starting JobProcessor with {} Event(s) queued", jobQueue.size());
        controllerThread = new Thread(()->{
            logger.debug("Started controllerThread");
            ForkJoinPool pool = ForkJoinPool.commonPool();

            while(!stopProcessing){
                while(!jobQueue.isEmpty()){
                    Tuple<Job, Event> tuple = jobQueue.poll();
                    Job job = tuple.getLeft();
                    Event evt = tuple.getRight();

                    if(job.canRun(ctx, evt)){
                        pool.submit(()->{
                            List<Event> result = job.run(ctx,evt);
                            synchronized (finishedQueue){
                                finishedQueue.add(result);
                                notifyAll();
                            }
                        });
                    }
                }

                synchronized (finishedQueue){
                    while (finishedQueue.isEmpty()){
                        try {
                            finishedQueue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                while (!finishedQueue.isEmpty()){
                    pushToFramework(finishedQueue.poll());
                }
                if(jobQueue.isEmpty()){
                    stopProcessing = true;
                }
            }
            logger.debug("Nothing more to process or processing canceled");
        });

        controllerThread.start();
    }

    private void pushToFramework(List<Event> events) {
        logger.debug("Notifying Framework about completed Events");
        notifyObservers(events);
        setChanged();
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

    public void waitForFinish(){
        boolean joined = false;
        while(!joined){
            try {
                controllerThread.join();
                joined = true;
            } catch (InterruptedException ignored) {
            }
        }
    }
}
