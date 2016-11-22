package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobProcessor;
import ch.hsr.maloney.processing.SimpleProcessor;
import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.storage.FileSystemMetadata;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.storage.PlainSource;
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
                new PlainSource(metadataStore)
        );
    }

    public void checkDependencies() {
        //TODO: Not necessary as of now, but later
    }

    public void startWithDisk(String fileName) {
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
        Event event = new Event("newDiskImage", "ch.hsr.maloney.core", uuid);

        registeredJobs.forEach((job -> {
            //TODO check whether Job is interested in this event or not
            jobProcessor.enqueue(job, event);
        }));

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
    public void update(Observable o, Event arg) {
        throw new UnsupportedOperationException();
    }
}
