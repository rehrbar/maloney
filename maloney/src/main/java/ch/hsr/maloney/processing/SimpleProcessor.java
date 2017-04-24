package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import ch.hsr.maloney.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// TODO phrasing!
public class SimpleProcessor extends JobProcessor {
    private final Logger logger;
    private final Queue<JobExecution> jobQueue; //TODO replace with better Queueing structure (persistent)
    private Context ctx;

    public SimpleProcessor(Context ctx) {
        logger = LogManager.getLogger();
        this.ctx = ctx;
        jobQueue = new LinkedList<>();
    }

    @Override
    public void start() {
        logger.debug("Starting Processing");
        //TODO add the stuff below into a thread(pool)
        while(!jobQueue.isEmpty()){
            // while there are still jobs to be run...
            JobExecution jobExecution = jobQueue.poll();
            Job job = jobExecution.getJob();
            Event evt = jobExecution.getTrigger();

            if(job.shouldRun(ctx, evt)){
                if(job.canRun(ctx, evt)){
                    try {
                        jobExecution.setResults(job.run(ctx, evt));
                        notifyInterested(jobExecution);
                    } catch (JobCancelledException e) {
                        // TODO store failed executions somewhere
                        logger.info("Job {} cancelled the execution of event {}: file {}", e.getJobName(), e.getEventId(), e.getFileId());
                    } catch (RuntimeException  e){
                        logger.error("Job processing failed.", e);
                    }
                }
            } else {
                // Finish job without producing a result/events.
                notifyInterested(jobExecution);
            }
        }
        logger.debug("Nothing more to process");
    }

    private void notifyInterested(JobExecution jobExecution) {
        setChanged();
        notifyObservers(jobExecution);
    }


    @Override
    public void stop() {
        //TODO: Not necessary as of now, because it's sequential (single Process). But in the future maybe?
    }

    @Override
    public void enqueue(JobExecution jobExecution) {
        logger.debug("Job '{}' enqueued with new event '{}'", jobExecution.getJob().getJobName(), jobExecution.getTrigger().getName());
        jobQueue.add(jobExecution);
    }
}
