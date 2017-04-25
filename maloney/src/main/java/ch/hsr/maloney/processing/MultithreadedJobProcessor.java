package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.*;
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

        if (job.shouldRun(ctx, evt)) {
            if (job.canRun(ctx, evt)) {
                try {
                    semaphore.acquire();
                    pool.submit(() -> {
                        try {
                            jobExecution.setResults(job.run(ctx, evt));
                            notifyInterested(jobExecution);
                        } catch (JobCancelledException e) {
                            // TODO store failed executions somewhere
                            logger.info("Job {} cancelled the execution of event {}: file {}", e.getJobName(), e.getEventId(), e.getFileId());
                        } catch (RuntimeException e) {
                            logger.error("Job processing failed.", e);
                        }

                        semaphore.release();
                    });
                } catch (InterruptedException e) {
                    logger.error("Could not schedule new Job", e);
                }
            }
        } else {
            // Finish job without producing a result/events.
            notifyInterested(jobExecution);
        }
    }

    private void notifyInterested(JobExecution jobExecution) {
        setChanged();
        notifyObservers(jobExecution);
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
            logger.error("Waiting for finish was interrupted", e);
        }
    }

}
