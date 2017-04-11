package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
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
    private static final int MAXCONCURRENTJOBS = Integer.MAX_VALUE;
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
        Event evt = jobExecution.getTrigger();

        if (job.canRun(ctx, evt)) {
            try {
                logger.debug("Trying to aqcuire token");
                semaphore.acquire();
                logger.debug("Acquired token");
                pool.submit(() -> {
                    jobExecution.setResults( job.run(ctx, evt));
                    setChanged();
                    notifyObservers(jobExecution);
                    semaphore.release();
                    logger.debug("Released token");
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
    public synchronized void enqueue(JobExecution jobExecution) {
        logger.debug("Enqueued '{}' to '{}'", jobExecution.getTrigger().getName(), jobExecution.getJob().getJobName());
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

}
