package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.*;
import ch.hsr.maloney.util.CustomClassLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;

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
        Framework framework = new Framework();
        // TODO assign configuration specific to framework

        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        CustomClassLoader myClassLoader = new CustomClassLoader(urlClassLoader);

        try {
            // Extracts the plugins folder which is a sibling of the application folder (like libs)
            String path = FrameworkController.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File pluginFolder = new File(path).getParentFile().toPath().resolveSibling("plugins").toFile();
            File[] jars = pluginFolder.listFiles((dir, filename) -> filename.endsWith(".jar"));
            if(jars != null){
                for (File jar : jars) {
                    myClassLoader.addURL(jar.toURI().toURL());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // load all implementations of interface Job using SPI
        Iterator<Job> iter = ServiceLoader.load(Job.class, myClassLoader).iterator();
        // configure jobs and assign them
        while (iter.hasNext()) {
            // TODO assign configuration to each job
            Job job = iter.next();
            job.setJobConfig(config.getJobConfigurationMap().getOrDefault(job.getJobName(),""));
            System.out.println("Registering job " + job.getJobName());
            framework.register(job);
        }

        framework.start();
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