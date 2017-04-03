package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.*;
import ch.hsr.maloney.util.CustomClassLoader;
import ch.hsr.maloney.util.ProgressInfoType;
import ch.hsr.maloney.util.ProgressTracker;
import ch.hsr.maloney.util.SimpleProgressTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * @author oniet
 * Creates the Jobs which are to be run, registers them at the Framework, and then starts the Application.
 */
public class FrameworkController {

    private static final int START_TIME = 0;
    private static final int CONSOLE_UPDATE_FREQUENCY_IN_SECONDS = 5;

    private static ClassLoader myClassLoader;
    private final Logger logger;
    private static final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();

    public FrameworkController() {
        logger = LogManager.getLogger();
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

        Framework framework = new Framework(progressTracker);
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

    private static void scheduleProgressTracker(final ProgressTracker progressTracker) {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder();
                for (ProgressInfoType infoType: ProgressInfoType.values()) {
                    stringBuilder
                            .append(infoType.getFieldDescription())
                            .append(":\t\t")
                            .append(progressTracker.getProcessedAmount(infoType))
                            .append("\n\r");
                }
                System.out.println(stringBuilder.toString());
            }
        }, START_TIME, CONSOLE_UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void run(FrameworkConfiguration config) {
        ProgressTracker progressTracker = new SimpleProgressTracker();

        Framework framework = new Framework(progressTracker);
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

        Framework framework = new Framework(progressTracker);
        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(hashSetPath);
        framework.register(importRdsHashSetJob);

        framework.start();
    }
}