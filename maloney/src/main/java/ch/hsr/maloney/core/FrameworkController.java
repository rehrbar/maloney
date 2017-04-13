package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.*;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.LocalDataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.*;

/**
 * @author oniet
 * Creates the Jobs which are to be run, registers them at the Framework, and then starts the Application.
 */
public class FrameworkController {

    private static final int START_TIME = 0;
    private static final int UPDATE_FREQUENCY_IN_SECONDS = 3;
    private static final int THREE_TABULATORS = 16;

    private static ClassLoader myClassLoader;
    private static final Logger logger = LogManager.getLogger();;
    private static final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();

    public FrameworkController() {
        if (myClassLoader == null) {
            try {
                myClassLoader = CustomClassLoader.createPluginLoader();
            } catch (MalformedURLException e) {
                logger.warn("Failed to detect plugins. Proceeding without plugins.", e);
                myClassLoader = ClassLoader.getSystemClassLoader();
            }
        }
    }

    public static void run(String imagePath) {
        ProgressTracker progressTracker = new SimpleProgressTracker();
        Context ctx = initializeContext(null, progressTracker, null);
        Framework framework = new Framework(ctx);

        // TODO rework how jobs are added and configured.
        DiskImageJob diskImageJob = new DiskImageJob();
        diskImageJob.setJobConfig(imagePath);
        framework.register(diskImageJob);
        framework.register(new TSKReadImageJob());
        framework.register(new CalculateHashesJob());
        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(""); // TODO set path from run parameters.
        framework.register(importRdsHashSetJob);

        scheduleProgressTracker(progressTracker);

        framework.start();

        scheduledThreadPoolExecutor.shutdown();
    }

    /**
     *
     * @param metadataStore     Specify DataStore, if null the default is taken (Elasticsearch)
     * @param progressTracker   Specify ProgressTracker, if null the default is taken (SimpleProgressTracker)
     * @param dataSource        Specify DataSource, if null the default is taken (LocalDataSource)
     * @return                  Created Context with specified parameters
     */
    private static Context initializeContext(MetadataStore metadataStore, ProgressTracker progressTracker, DataSource dataSource) {
        if(metadataStore == null){
            try {
                metadataStore = new ch.hsr.maloney.storage.es.MetadataStore();
            } catch (UnknownHostException e) {
                logger.fatal("Elasticsearch host not found. Terminating...", e);
                System.exit(0);
            }
        }

        if(progressTracker == null){
            progressTracker = new SimpleProgressTracker();
        }

        if(dataSource == null){
            dataSource = new LocalDataSource(metadataStore);
        }
        return new Context(
                metadataStore,
                progressTracker,
                dataSource
        );
    }

    private static void scheduleProgressTracker(final ProgressTracker progressTracker) {
        final long startTime = System.currentTimeMillis();

        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (String type: progressTracker.getTypes()) {
                stringBuilder
                        .append(type);
                if(type.length() > THREE_TABULATORS){
                    stringBuilder.append(":\t");
                } else {
                    stringBuilder.append(":\t\t");
                }
                stringBuilder
                        .append(progressTracker.getProcessedAmount(type))
                        .append("\n\r");
            }

            //TODO time estimation
            //TODO use counter class
            //TODO aging of time calculations
            int processing = progressTracker.getProcessedAmount(ProgressInfoType.NEW_EVENT.toString());
            int finished = progressTracker.getProcessedAmount(ProgressInfoType.PROCESSED_EVENT.toString());

            final long currentTime = System.currentTimeMillis();

            double speed = (finished)/(currentTime - startTime); //Tasks per milliseconds

            if(speed == 0){
                speed = 0.001; // Low-Ball so that the estimation is higher at first
            }

            long remainingTime = (long)(processing / speed);

            LocalDateTime eta = LocalDateTime.now().withFieldAdded(DurationFieldType.minutes(),1);

            eta.withFieldAdded(DurationFieldType.millis(), (int)remainingTime);

            stringBuilder.append("ETA: ").append(eta.toString("dd.MM.yyyy HH:mm"));

            System.out.println(stringBuilder.toString());
        }, START_TIME, UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }


    public void run(FrameworkConfiguration config) {
        ProgressTracker progressTracker = new SimpleProgressTracker();
        Context ctx = initializeContext(null, progressTracker, null);
        Framework framework = new Framework(ctx);
        // TODO configure framework with this configuration

        logger.info("Starting with configuration");

        // load all implementations of interface Job using SPI
        Iterator<Job> iter = ServiceLoader.load(Job.class, myClassLoader).iterator();
        // configure jobs and assign them
        while (iter.hasNext()) {
            // TODO only register jobs which are configured to run
            Job job = iter.next();
            job.setJobConfig(config.getJobConfigurationMap().getOrDefault(job.getJobName(), ""));
            logger.debug("Registering job " + job.getJobName());
            framework.register(job);
        }

        scheduleProgressTracker(progressTracker);

        framework.start();
        logger.info("Framework has finished");
    }

    public static void runHashSet(String hashSetPath) {
        // TODO replace through run(FrameworkConfiguration config)
        ProgressTracker progressTracker = new SimpleProgressTracker();
        Context ctx = initializeContext(null, progressTracker, null);
        Framework framework = new Framework(ctx);

        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(hashSetPath);
        framework.register(importRdsHashSetJob);

        framework.start();
    }
}