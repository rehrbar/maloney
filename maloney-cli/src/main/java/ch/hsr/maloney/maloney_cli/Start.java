package ch.hsr.maloney.maloney_cli;

import ch.hsr.maloney.core.FrameworkConfiguration;
import ch.hsr.maloney.core.FrameworkController;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.IOException;
import java.nio.file.Path;

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

            if (line.hasOption("c")) {
                // TODO merge stored configuration with command line parameters
                FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.loadFromFile(line.getOptionValue("c", "./configuration.json"));
                FrameworkController controller = new FrameworkController(frameworkConfiguration, line.getOptionValue("id"));

                addFileLogger(controller.getCaseDirectory());

                if (line.hasOption("clear-events")) {
                    controller.clearEvents();
                } else {
                    if (controller.hasEvents()) {
                        logger.info("Not finished events were found, which will be restarted."
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

    /**
     * Saves a log file to provided directory.
     * This configuration is lost after the configuration file is reloaded.
     *
     * @param target The target directory.
     */
    private static void addFileLogger(Path target) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, null, config, null,
                null, false, false, null, null);
        FileAppender appender = FileAppender.newBuilder()
                .withFileName(target.resolve("maloney.log").toString())
                .withName("maloney-file")
                .withLayout(layout)
                .build();
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[]{ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, "ch.hsr.maloney",
                "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("ch.hsr.maloney", loggerConfig);
        ctx.updateLoggers();
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("maloney", options, true);
    }
}
