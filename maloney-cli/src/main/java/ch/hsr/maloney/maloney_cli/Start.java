package ch.hsr.maloney.maloney_cli;

import ch.hsr.maloney.core.FrameworkConfiguration;
import ch.hsr.maloney.core.FrameworkController;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
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

        CommandLineParser parser = new DefaultParser();

        options = new Options();
        options.addOption("v", "verbose", false, "enables verbose output");
        options.addOption("id", "identifier", true, "case identifier used for operations");
        options.addOption("c", "configuration", true, "configuration to load");
        options.addOption("sc", "save-configuration", true, "saves an example configuration");
        options.addOption("h", "help", false, "prints this help");
        options.addOption(null, "clear-events", false, "clears stored events before start");
        options.addOption("q","query", true,"query all stored meta-data");
        options.addOption(null,"filter", true,"filter for output");

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                printHelp();
                return;
            }

            // Preparing the logger.
            if (line.hasOption("v")) {
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

            if(line.hasOption("q") && line.hasOption("c")){
                FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.loadFromFile(line.getOptionValue("c", "./configuration.json"));
                FrameworkController controller = new FrameworkController(frameworkConfiguration, line.getOptionValue("id"));

                controller.query(
                        line.getOptionValue("q", ""),
                        line.getOptionValue("filter", null)
                );

                System.exit(0);
                return;
            }

            if (line.hasOption("c")) {
                // TODO merge stored configuration with command line parameters
                FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.loadFromFile(line.getOptionValue("c", "./configuration.json"));
                FrameworkController controller = new FrameworkController(frameworkConfiguration, line.getOptionValue("id"));

                if (line.hasOption("clear-events")) {
                    controller.clearEvents();
                } else {
                    if (controller.hasEvents()) {
                        logger.info("Unfinished events were found: Application will resume."
                                + "To clear pending events, use switch --{} instead.", "clear-events");
                    }
                }

                controller.run();

                System.exit(0);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("\"This is not the druid you are looking for.\"");
            System.out.println("Could not parse arguments: " + e.getMessage());
            printHelp();
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("maloney", options, true);
    }
}
