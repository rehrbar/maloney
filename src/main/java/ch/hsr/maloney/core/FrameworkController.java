package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.CalculateHashesJob;
import ch.hsr.maloney.processing.DiskImageJob;
import ch.hsr.maloney.processing.ImportRdsHashSetJob;
import ch.hsr.maloney.processing.TSKReadImageJob;

/**
 * @author oniet
 *
 * Creates the Jobs which are to be run, registers them at the Framework, and then starts the Application.
 */
public class FrameworkController {
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

    public static void run(FrameworkConfiguration config) {
        // TODO implement this
    }

    public static void runHashSet(String hashSetPath) {
        // TODO replace through run(FrameworkConfiguration config)
        Framework framework = new Framework();
        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(hashSetPath);
        framework.register(importRdsHashSetJob);

        framework.start();
    }
}