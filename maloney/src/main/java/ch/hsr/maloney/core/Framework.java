package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.MultithreadedJobProcessor;
import ch.hsr.maloney.processing.TSKReadImageJob;
import ch.hsr.maloney.storage.EventStore;
import ch.hsr.maloney.storage.LocalDataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.*;

/**
 * @author oniet
 *         <p>
 *         Framework for the whole applicaton:
 *         - Initializes all needed classes (DataSource, MetadataStore, ProgressTracker)
 *         - Checks dependencies on registered Jobs on initialization
 *         - Enqueues new Events to interested Jobs
 *         - ATM: Creates new Event on start (should be moved to a seperate Job...)
 */
public class Framework implements Observer {
    public static final String EVENT_ORIGIN = "ch.hsr.maloney.core";
    private final Logger logger;
    private MultithreadedJobProcessor jobProcessor;
    protected Context context;
    private EventStore eventStore; //TODO Better Queue with nice persistence
    private List<Job> registeredJobs;

    public Framework() {
        this.logger = LogManager.getLogger();
        initializeContext();
        this.registeredJobs = new LinkedList<>();
        this.eventStore = new EventStore();
        this.jobProcessor = new MultithreadedJobProcessor(context);
        jobProcessor.addObserver(this);
    }

    protected void initializeContext() {
        MetadataStore metadataStore = null;
        try {
            metadataStore = new ch.hsr.maloney.storage.es.MetadataStore();
        } catch (UnknownHostException e) {
            logger.fatal("Elasticsearch host not found. Terminating...", e);
            System.exit(0);
        }
        this.context = new Context(
                metadataStore,
                null, //TODO Implement and add Progress Tracker
                new LocalDataSource(metadataStore)
        );
    }

    /**
     * Checks whether all registered Jobs can be run by looking through all produced and required Events.
     *
     * @throws UnrunnableJobException If one or multiple registered Jobs cannot be run, this Exception is thrown
     */
    public void checkDependencies() throws UnrunnableJobException {
        Set<String> availableEvents = new HashSet<>();
        Set<Job> unresolvedDependencies = new HashSet<>(registeredJobs);

        logger.debug("Checking if registered Jobs can be run...");

        availableEvents.add(FrameworkEventNames.STARTUP);
        // TODO add event names of recovered events

        LinkedList<Job> runnableJobs = new LinkedList<>();
        runnableJobs.add(new TSKReadImageJob());
        // Added some random Job that gets cleared out
        // just so that the following while loop can be started

        while (runnableJobs.size() > 0) {
            runnableJobs.clear();
            for (Job job : unresolvedDependencies) {
                if (availableEvents.containsAll(job.getRequiredEvents())) {
                    availableEvents.addAll(job.getProducedEvents());
                    runnableJobs.add(job);
                }
            }
            unresolvedDependencies.removeAll(runnableJobs);
        }

        if (unresolvedDependencies.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error with following Jobs:\r\n");
            unresolvedDependencies.forEach(job -> {
                stringBuilder.append(job.getJobName());
                stringBuilder.append("\r\n");
            });
            logger.error(stringBuilder.toString());
            throw new UnrunnableJobException(stringBuilder.toString());
        }
        logger.debug("... Job dependencies to each other look good.");
    }

    /**
     * Starts the framework.
     */
    public void start() {
        if(eventStore.hasEvents()){
            // TODO ask user whether to restore events or remove them.
            // TODO find another way to prevent startup event or introduce some new ones (RESTART/FRESHSTART)
            Collection<Event> recoveredEvents = eventStore.getEvents();
            recoveredEvents.forEach(event -> enqueueToInterestedJobs(event));
        } else {
            enqueueToInterestedJobs(new Event(FrameworkEventNames.STARTUP, EVENT_ORIGIN, null));
        }
        try {
            checkDependencies();
        } catch (UnrunnableJobException e) {
            logger.fatal("Cannot run all Jobs", e);
            return;
        }
        long startTime = System.currentTimeMillis();
        jobProcessor.start();
        jobProcessor.waitForFinish();
        //TODO wait for abort command or for the application finish Event
        logger.info("Completion time: {}", System.currentTimeMillis() - startTime);
    }

    /**
     * Register a Job on the Framework to be run if the required Event is created.
     *
     * @param job Job to be registered and run when required Event is created.
     */
    public void register(Job job) {
        registeredJobs.add(job);
    }

    public void unregister(Job job) {
        if (registeredJobs.contains(job)) {
            registeredJobs.remove(job);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof JobExecution) {
            JobExecution jobExecution = (JobExecution) arg;
            enqueueToInterestedJobs(jobExecution.getResults());
            eventStore.remove(jobExecution);
            return;
        }
        throw new IllegalArgumentException("I just don't know, what to doooooo with this type... \uD83C\uDFB6");
    }


    private void enqueueToInterestedJobs(Event evt) {
        this.enqueueToInterestedJobs(new LinkedList<Event>(){{push(evt);}});
    }

    private void enqueueToInterestedJobs(Collection<Event> events) {
        Collection<JobExecution> plannedExecutions = new LinkedList<>();
        for (Event evt : events) {
            registeredJobs.stream()
                    .filter(j -> j.getRequiredEvents().contains(evt.getName()))
                    .map(j -> new JobExecution(j, evt)).forEach(j -> plannedExecutions.add(j));
        }

        eventStore.add(plannedExecutions);
        plannedExecutions.forEach(j -> jobProcessor.enqueue(j));
    }

    /**
     * If registered Jobs cannot be run and the Framework realizes this inside checkDependencies(),
     * this Exception is thrown.
     */
    public class UnrunnableJobException extends Exception {
        public UnrunnableJobException() {
        }

        public UnrunnableJobException(String msg) {
            super(msg);
        }
    }
}
