package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobProcessor;
import ch.hsr.maloney.processing.SimpleProcessor;
import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.storage.FileSystemMetadata;
import ch.hsr.maloney.storage.LocalDataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.EventObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by olive_000 on 25.10.2016.
 */
public class Framework implements EventObserver {
    final Logger logger;
    private JobProcessor jobProcessor;
    private Context context;
    private Queue<Event> eventQueue; //TODO Better Queue with nice persistence
    private List<Job> registeredJobs;

    private final String NewDiskImageEventName = "newDiskImage";

    public Framework() {
        this.logger = LogManager.getLogger();
        initializeContext();
        this.registeredJobs = new LinkedList<>();
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.jobProcessor = new SimpleProcessor(context);
    }

    private void initializeContext() {
        MetadataStore metadataStore = null;
        try {
            metadataStore = new ch.hsr.maloney.storage.es.MetadataStore();
        } catch (UnknownHostException e) {
            logger.fatal("Elasticsearch host not found. Terminating...", e);
            System.exit(0);
        }
        this.context = new Context(
                metadataStore,
                null, //TODO Implement adn add Progress Tracker
                new LocalDataSource(metadataStore)
        );
    }

    public void checkDependencies() {
        Set<String> availableEvents = new HashSet<>();
        Set<Job> unresolvedDependencies = new HashSet<>(registeredJobs);

        //TODO remove this as soon as startWithDisk is a registered Job within the Framework
        availableEvents.add(NewDiskImageEventName);

        boolean addedSome = true;

        while(addedSome){
            addedSome = false;
            for(Job job : registeredJobs){
                if(availableEvents.containsAll(job.getRequiredEvents())){
                    addedSome = true;
                    availableEvents.addAll(job.getProducedEvents());
                    unresolvedDependencies.remove(job);
                }
            }
        }
    }

    public void startWithDisk(String fileName) {
        //TODO extract to Job
        UUID uuid = context.getDataSource().addFile(null, new FileExtractor() {

            private Path path = Paths.get(fileName);

            @Override
            public boolean useOriginalFile() {
                return true;
            }

            @Override
            public Path extractFile() {
                return path;
            }

            @Override
            public FileSystemMetadata extractMetadata() {
                // TODO supply some metadata about the image. E.g. creationDate, name, etc.
                FileSystemMetadata metadata = new FileSystemMetadata();
                metadata.setFileName(path.getFileName().toString());
                return metadata;
            }

            @Override
            public void cleanup() {
                // nothing to cleanup yet
            }
        });

        Event event = new Event(NewDiskImageEventName, "ch.hsr.maloney.core", uuid);

        enqueueToInterestedJobs(event);

        jobProcessor.start();
    }

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
        if (arg instanceof Event) {
            update(o, (Event) arg);
        } else {
            throw new IllegalArgumentException("I just don't know, what to doooooo with this type... \uD83C\uDFB6");
        }
    }

    @Override
    public void update(Observable o, Event evt) {
        enqueueToInterestedJobs(evt);
    }

    private void enqueueToInterestedJobs(Event evt) {
        registeredJobs.forEach((job)->{
            if(job.getRequiredEvents().contains(evt.getName())){
                jobProcessor.enqueue(job, evt);
            }
        });
    }
}
