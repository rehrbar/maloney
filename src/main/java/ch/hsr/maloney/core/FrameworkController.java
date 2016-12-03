package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.CalculateHashesJob;
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
        framework.register(new TSKReadImageJob());
        framework.register(new CalculateHashesJob());
        ImportRdsHashSetJob importRdsHashSetJob = new ImportRdsHashSetJob();
        importRdsHashSetJob.setJobConfig(""); // TODO set path from run parameters.
        framework.register(importRdsHashSetJob);

        framework.startWithDisk(imagePath);
    }
}