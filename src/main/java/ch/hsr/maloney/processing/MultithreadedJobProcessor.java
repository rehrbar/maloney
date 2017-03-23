package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author oniet
 *         <p>
 *         Handles the running of jobs.
 *         <p>
 *         Uses Threadpool (ForkJoinPool) and Futures and to run Jobs and the Observer Pattern to notify the Framwork.
 */
public class MultithreadedJobProcessor extends JobProcessor {
    private static final int CONCURRENTJOBS = 1000;
    private final Logger logger;
    private final Queue<JobExecution> readyJobs; //TODO replace with better Queueing structure (persistent)

    private Context ctx;

    private Thread controllerThread;
    private boolean stopProcessing;
    private ForkJoinPool pool;
    Semaphore semaphore = new Semaphore(CONCURRENTJOBS);

    public MultithreadedJobProcessor(Context ctx) {
        logger = LogManager.getLogger();
        this.ctx = ctx;
        readyJobs = new ConcurrentLinkedQueue<>();
        pool = new ForkJoinPool();

        stopProcessing = false;
    }

    @Override
    public void start() {
        logger.debug("Starting JobProcessor with {} Event(s) queued", readyJobs.size());
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        controllerThread = new Thread(() -> {
            logger.debug("Started controllerThread");

            while (!stopProcessing) {
                while (!readyJobs.isEmpty() && !stopProcessing) {
                    JobExecution jobExecution = readyJobs.poll();
                    Job job = jobExecution.getJob();
                    Event evt = jobExecution.getEvent();

                    if (job.canRun(ctx, evt)) {

                        try {
                            logger.debug("Trying to aqcuire token");
                            semaphore.acquire();
                            logger.debug("Acquired token");
                            pool.submit(() -> {
                                List<Event> result = job.run(ctx, evt);
                                if (result != null && !result.isEmpty()) {
                                    setChanged();
                                    notifyObservers(result);
                                }
                                semaphore.release();
                                logger.debug("Released token");
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            logger.debug("Nothing more to process or processing canceled");
        });

        controllerThread.start();
        semaphore.release();
    }

    @Override
    public void stop() {
        if (controllerThread.getState() != Thread.State.TERMINATED) {
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
        //TODO enqueue directly in pool
        synchronized (readyJobs) {
            readyJobs.add(new JobExecution(job, event));
            readyJobs.notifyAll();
        }
    }

    public void waitForFinish() {
        logger.debug("Waiting for JobProcessor to finish...");
        try {
            semaphore.acquire(CONCURRENTJOBS);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
