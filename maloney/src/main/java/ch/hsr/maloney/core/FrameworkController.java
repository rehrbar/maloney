package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.*;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.EventStore;
import ch.hsr.maloney.storage.LocalDataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author oniet
 * Creates the Jobs which are to be run, registers them at the Framework, and then starts the Application.
 */
public class FrameworkController {

    private static final int START_TIME = 0;
    private static final int UPDATE_FREQUENCY_IN_SECONDS = 3;
    private static final int RELEVANT_CYCLES = 10;
    private static final int THREE_TABULATORS = 16;

    private static ClassLoader myClassLoader;
    private static final Logger logger = LogManager.getLogger();;
    private static final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
    private static EventStore eventStore;
    private final boolean isRestarting;
    private boolean isShuttingDown;
    private Framework framework;

    public FrameworkController() {
        if (myClassLoader == null) {
            try {
                myClassLoader = CustomClassLoader.createPluginLoader();
            } catch (MalformedURLException e) {
                logger.warn("Failed to detect plugins. Proceeding without plugins.", e);
                myClassLoader = ClassLoader.getSystemClassLoader();
            }
        }
        // TODO allow another start after shutdown was called?
        eventStore = new EventStore();
        isRestarting = eventStore.hasEvents();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
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
        ETACalculator etaCalculator = new ETACalculator(RELEVANT_CYCLES);

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

            //time estimation

            int processing = progressTracker.getProcessedAmount(ProgressInfoType.TASK_QUEUED.toString());
            int finished = progressTracker.getProcessedAmount(ProgressInfoType.TASK_FINISHED.toString());

            etaCalculator.addMeasurement(processing, finished, System.currentTimeMillis());

            LocalDateTime eta = etaCalculator.getETA();

            stringBuilder
                    .append("\r\n")
                    .append("Average Speed: ")
                    .append(String.format("%.2f", etaCalculator.getAverageSpeed() * 1000)).append(" Tasks/Second\r\n")
                    .append("ETA: ").append(eta == null ? "n/a" : eta.toString("yyyy-MM-dd HH:mm")).append("\r\n");

            System.out.println(stringBuilder.toString());
        }, START_TIME, UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }


    public void run(FrameworkConfiguration config) {
        ProgressTracker progressTracker = new SimpleProgressTracker();
        Context ctx = initializeContext(null, progressTracker, null);
        framework = new Framework(eventStore, ctx);
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
        // TODO handle not finished executions
    }

    public boolean isRestarting() {
        return isRestarting;
    }

    public void clearEvents(){
        eventStore.clear();
    }

    private synchronized void shutdown(){
        // prevent shutting down twice
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;
        logger.info("Initializing shutdown...");
        scheduledThreadPoolExecutor.shutdown();
        framework.shutdown();
        eventStore.close();
        logger.info("Shutdown complete");
    }
}