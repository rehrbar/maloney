package ch.hsr.maloney.maloney_cli;

import ch.hsr.maloney.core.FrameworkConfiguration;
import ch.hsr.maloney.core.FrameworkController;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.IOException;

/**
 * Entry point for the command line interface of Maloney.
 */
public class Start {

    private static Options options;

    public static void main(String[] args) {
        // Required override due to building problems of TSK on Windows 7 with .NET Framework >= 4.5 or newer Windows versions.
        // All libraries need to be inside java.library.path
        // Example: -Djava.library.path="C:\libs"
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.loadLibrary("zlib");
            System.loadLibrary("libewf");
            System.loadLibrary("libvmdk");
            System.loadLibrary("libvhdi");
            System.loadLibrary("libtsk_jni");
        }
        // TODO convert args to hash map to make them easier available

        CommandLineParser parser = new DefaultParser();

        options = new Options();
        options.addOption("v", "verbose", false, "enables verbose output");
        options.addOption("c", "configuration", true, "configuration to load");
        options.addOption("sc", "save-configuration", true, "saves an example configuration");
        options.addOption("h", "help", false, "prints this help");
        options.addOption("rds", true, "indexes an rds hash set"); // TODO replace with job configuration

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                printHelp();
                return;
            }

            // Preparing the logger.
            if(line.hasOption("v")){
                // TODO test if this is working properly.
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                Configuration config = ctx.getConfiguration();
                LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                loggerConfig.setLevel(Level.DEBUG);
                ctx.updateLoggers();
            }
            Logger logger = LogManager.getLogger();
            logger.info("Hello world");
            logger.debug("now in debugging mode");

            if (line.hasOption("sc")) {
                // TODO add generator for example configuration
                new FrameworkConfiguration().saveToFile(line.getOptionValue("sc"));
                return;
            }

            if (line.hasOption("c")) {
                // TODO allow overrides of FrameworkConfiguration
                // TODO pass working directory
                // TODO pass job configurations
                // TODO pass jobs to execute
                FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.loadFromFile(line.getOptionValue("c", "./configuration.json"));
                FrameworkController.run(frameworkConfiguration);
                return;
            }
            if (line.hasOption("rds")) {
                // TODO replace with job configuration
                FrameworkController.runHashSet(line.getOptionValue("rds"));
                return;
            }

            if(line.getArgList().size()>0) {
                // TODO rplace this old behaviour with an image file through the configurable version.
                FrameworkController.run(line.getArgs()[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Could not parse arguments: " + e.getMessage());
            printHelp();
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("maloney", options, true);
    }
}
