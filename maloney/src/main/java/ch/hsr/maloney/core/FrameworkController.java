package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.*;
import ch.hsr.maloney.storage.EventStore;
import ch.hsr.maloney.util.CustomClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author oniet
 * Creates the Jobs which are to be run, registers them at the Framework, and then starts the Application.
 */
public class FrameworkController {

    private static ClassLoader myClassLoader;
    private final Logger logger;
    private final EventStore eventStore;
    private final boolean isRestarting;

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
        eventStore = new EventStore();
        isRestarting = eventStore.hasEvents();
    }

    public static void run(String imagePath) {
        Framework framework = new Framework();
        // TODO rework how jobs are added and configured.
        DiskImageJob diskImageJob = new DiskImageJob();
        diskImageJob.setJobConfig(imagePath);
        framework.register(diskImageJob);
        framework.register(new TSKReadImageJob());
        framework.register(new CalculateHashesJob());
        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(""); // TODO set path from run parameters.
        framework.register(importRdsHashSetJob);

        framework.start();
    }

    public void run(FrameworkConfiguration config) {
        Framework framework = new Framework(eventStore);
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

        framework.start();
        logger.info("Framework has finished");
    }

    public static void runHashSet(String hashSetPath) {
        // TODO replace through run(FrameworkConfiguration config)
        Framework framework = new Framework();
        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(hashSetPath);
        framework.register(importRdsHashSetJob);

        framework.start();
    }

    public boolean isRestarting() {
        return isRestarting;
    }

    public void clearEvents(){
        eventStore.clear();
    }
}