package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

/**
 * @author oniet
 *         <p>
 *         Handles the running of jobs.
 *         <p>
 *         Uses threads in a thread pool (ForkJoinPool) to run Jobs and the Observer Pattern to notify the Framwork.
 */
public class MultithreadedJobProcessor extends JobProcessor {
    private static final int MAXCONCURRENTJOBS = 1000;
    private final Logger logger;
    private final Queue<JobExecution> readyJobs; //TODO replace with better Queueing structure (persistent)
    private final Context ctx;
    private final ForkJoinPool pool;
    private final Semaphore semaphore = new Semaphore(MAXCONCURRENTJOBS);
    private volatile boolean isStarted = false;

    public MultithreadedJobProcessor(Context ctx) {
        logger = LogManager.getLogger();
        this.ctx = ctx;
        readyJobs = new LinkedList<>();
        pool = new ForkJoinPool();
    }

    @Override
    public synchronized void start() {
        logger.debug("Starting JobProcessor with {} Event(s) queued", readyJobs.size());
        isStarted = true;

        while (!readyJobs.isEmpty()) {
            JobExecution jobExecution = readyJobs.poll();
            putInPool(jobExecution);
        }
    }

    private void putInPool(JobExecution jobExecution) {
        Job job = jobExecution.getJob();
        Event evt = jobExecution.getEvent();

        if (job.canRun(ctx, evt)) {
            try {
                semaphore.acquire();
                pool.submit(() -> {
                    List<Event> result = job.run(ctx, evt);
                    if (result != null && !result.isEmpty()) {
                        setChanged();
                        notifyObservers(result);
                        ctx.getProgressTracker().processInfo(
                                new ProgressInfo(ProgressInfoType.NEW_EVENT,result.size())
                        );
                    }
                    ctx.getProgressTracker().processInfo(new ProgressInfo(ProgressInfoType.PROCESSED_EVENT,1));
                    semaphore.release();
                });
            } catch (InterruptedException e) {
                logger.error("Could not schedule new Job",e);
            }
        }
    }

    @Override
    public void stop() {
        pool.shutdown();
    }

    @Override
    public synchronized void enqueue(Job job, Event event) {
        logger.debug("Enqueued '{}' to '{}'", event.getName(), job.getJobName());
        JobExecution jobExecution = new JobExecution(job, event);
        if (isStarted && !pool.isShutdown()) {
            putInPool(jobExecution);
        } else {
            readyJobs.add(jobExecution);
        }
    }

    public void waitForFinish() {
        logger.debug("Waiting for JobProcessor to finish...");
        try {
            semaphore.acquire(MAXCONCURRENTJOBS);
            logger.debug("Nothing more to process or processing canceled");
            semaphore.release(MAXCONCURRENTJOBS);
        } catch (InterruptedException e) {
            logger.error("Waiting for finish was interrupted",e);
        }
    }

    class JobExecution extends Tuple<Job, Event> {
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
