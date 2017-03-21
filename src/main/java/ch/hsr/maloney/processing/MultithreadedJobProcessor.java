package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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
    private final Queue<JobExecution> readyJobs; //TODO replace with better Queueing structure (persistent)
    private final Queue<JobExecution> runningJobs; //TODO replace with better Queueing structure (persistent)

    private Context ctx;

    private Thread controllerThread;
    private boolean stopProcessing;

    public MultithreadedJobProcessor(Context ctx){
        logger = LogManager.getLogger();
        this.ctx = ctx;
        readyJobs = new LinkedList<>();
        runningJobs = new LinkedList<>();

        stopProcessing = false;
    }

    @Override
    public void start() {
        logger.debug("Starting JobProcessor with {} Event(s) queued", readyJobs.size());
        controllerThread = new Thread(()->{
            logger.debug("Started controllerThread");
            ForkJoinPool pool = ForkJoinPool.commonPool();

            while(!stopProcessing){
                while(!readyJobs.isEmpty()){
                    JobExecution jobExecution = readyJobs.poll();
                    Job job = jobExecution.getJob();
                    Event evt = jobExecution.getEvent();

                    if(job.canRun(ctx, evt)){
                        runningJobs.add(jobExecution);
                        //Future<List<Event>> task = pool.submit(()-> {
                        pool.submit(()-> {
                            //return job.run(ctx, evt);
                            notifyObservers(job.run(ctx, evt));
                            //int number = countObservers();
                            runningJobs.remove(jobExecution);
                            setChanged();
                        });
                    }
                }

                synchronized (readyJobs){
                    while(readyJobs.isEmpty()){
                        try {
                            readyJobs.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
            logger.debug("Nothing more to process or processing canceled");
        });

        controllerThread.start();
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
        synchronized (readyJobs){
            readyJobs.add(new JobExecution(job, event));
            readyJobs.notifyAll();
        }
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

    class JobExecution extends Tuple<Job, Event>{
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
