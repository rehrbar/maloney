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

            if(job.canRun(ctx, evt)){
                jobExecution.setResults(job.run(ctx, evt));
                setChanged();
                notifyObservers(jobExecution);
            }
        }
        logger.debug("Nothing more to process");
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
