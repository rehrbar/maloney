package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.EventStore;
import ch.hsr.maloney.storage.LocalDataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.*;
import ch.hsr.maloney.util.categorization.Category;
import ch.hsr.maloney.util.categorization.CategoryService;
import ch.hsr.maloney.util.query.SimpleQuery;
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
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author oniet
 * Creates the Jobs which are to be run, registers them at the Framework, and then starts the Application.
 */
public class FrameworkController {

    private static final int START_TIME = 0;
    private static final int UPDATE_FREQUENCY_IN_SECONDS = 3;
    private static final int RELEVANT_CYCLES = 10;
    private static final DateTimeFormatter CONFIG_FILE_FORMATTER = DateTimeFormatter.ofPattern("'config_'yyyyMMdd-HHmmss'.json'");

    private static ClassLoader myClassLoader;
    private static final Logger logger = LogManager.getLogger();;
    private static final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
    private boolean isShuttingDown;
    private Framework framework;
    private String caseIdentifier;
    private Path workingDirectory;
    private EventStore eventStore;
    private Path caseDirectory;
    private final FrameworkConfiguration configuration;

    public FrameworkController(FrameworkConfiguration configuration){
        this(configuration, null);
    }

    public FrameworkController(FrameworkConfiguration configuration, String caseIdentifier) {
        if (myClassLoader == null) {
            try {
                myClassLoader = CustomClassLoader.createPluginLoader();
            } catch (MalformedURLException e) {
                logger.warn("Failed to detect plugins. Proceeding without plugins.", e);
                myClassLoader = ClassLoader.getSystemClassLoader();
            }
        }

        // Configure controller
        this.configuration = configuration;
        setWorkingDirectory(configuration.getWorkingDirectory());
        setCaseIdentifier(caseIdentifier);

        addFileLogger(this.getCaseDirectory());
        backupConfiguration(configuration);

        eventStore = new EventStore(this.getCaseDirectory(), true);
        // TODO allow another start after shutdown was called?

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Creates a backup of the configuration in the current case directory.
     * The name will be extended with a timestamp, like config_20170504_133038.json
     * @param configuration Configuration to save.
     */
    private void backupConfiguration(FrameworkConfiguration configuration) {
        try {
            String fileName = ZonedDateTime.now().format(CONFIG_FILE_FORMATTER);
            String configPath = this.getCaseDirectory().resolve(fileName).toString();
            configuration.saveToFile(configPath);
            logger.info("Configuration backed up to {}", configPath);
        } catch (IOException e) {
            logger.warn("Failed to create backup of configuration.");
        }
    }

    /**
     *
     * @param metadataStore     Specify DataStore, if null the default is taken (Elasticsearch)
     * @param progressTracker   Specify ProgressTracker, if null the default is taken (SimpleProgressTracker)
     * @param dataSource        Specify DataSource, if null the default is taken (LocalDataSource)
     * @return                  Created Context with specified parameters
     */
    private Context initializeContext(MetadataStore metadataStore, ProgressTracker progressTracker, DataSource dataSource, CategoryService categoryService) {
        if(metadataStore == null){
            try {
                metadataStore = new ch.hsr.maloney.storage.es.MetadataStore(this.getCaseIdentifier());
            } catch (UnknownHostException e) {
                logger.fatal("Elasticsearch host not found. Terminating...", e);
                System.exit(0);
            }
        }

        if(progressTracker == null){
            progressTracker = new SimpleProgressTracker();
        }

        if(dataSource == null){
            dataSource = new LocalDataSource(metadataStore, this.getCaseDirectory());
        }
        if(categoryService == null){
            categoryService = new CategoryService();
        }

        Context context = new Context(
                metadataStore,
                progressTracker,
                dataSource,
                categoryService

        );
        context.setCaseIdentifier(caseIdentifier);
        return context;
    }

    private static void scheduleProgressTracker(final ProgressTracker progressTracker) {
        ETACalculator etaCalculator = new ETACalculator(RELEVANT_CYCLES);

        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (String type: progressTracker.getTypes()) {
                stringBuilder.append(String.format(
                        "%-20s %d\n\r", // %-20s results in a right padded string
                        type,
                        progressTracker.getProcessedAmount(type)
                ));
            }

            //time estimation

            int processing = progressTracker.getProcessedAmount(ProgressInfoType.TASK_QUEUED.toString());
            int finished = progressTracker.getProcessedAmount(ProgressInfoType.TASK_FINISHED.toString());

            etaCalculator.addMeasurement(processing, finished, System.currentTimeMillis());

            // TODO upgrade to java8 date and time (JSR-310)
            LocalDateTime eta = etaCalculator.getETA();

            stringBuilder
                    .append("\r\n")
                    .append("Average Speed: ")
                    .append(String.format("%.2f", etaCalculator.getAverageSpeed() * 1000)).append(" Tasks/Second\r\n")
                    .append("ETA: ").append(eta == null ? "n/a" : eta.toString("yyyy-MM-dd HH:mm")).append("\r\n");

            System.out.println(stringBuilder.toString());
        }, START_TIME, UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }


    public void run() {
        ProgressTracker progressTracker = new SimpleProgressTracker();
        Context ctx = initializeContext(null, progressTracker, null, null);
        framework = new Framework(eventStore, ctx);
        // TODO configure framework with this configuration

        logger.info("Starting with configuration");

        // load all implementations of interface Job using SPI
        Iterator<Job> iter = ServiceLoader.load(Job.class, myClassLoader).iterator();
        // configure jobs and assign them
        while (iter.hasNext()) {
            // TODO only register jobs which are configured to run
            Job job = iter.next();
            job.setJobConfig(this.configuration.getJobConfigurationMap().getOrDefault(job.getJobName(), null));
            logger.debug("Registering job " + job.getJobName());
            framework.register(job);
        }

        Iterator<Category> categoryIterator = ServiceLoader.load(Category.class, myClassLoader).iterator();
        while (categoryIterator.hasNext()) {
            Category category = categoryIterator.next();
            ctx.getCategoryService().addOrUpdateCategory(category);
        }

        scheduleProgressTracker(progressTracker);

        framework.start();
        // TODO handle not finished executions
    }

    public void query(String query, String filter){
        Context ctx = initializeContext(null, null, null, null);
        SimpleQuery q = new SimpleQuery(ctx.getMetadataStore());
        q.setFilter(filter);
        q.performQuery(System.out, query);
    }

    public boolean hasEvents() {
        return eventStore.hasEvents();
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

        // Possible that framework is not initialized due to misconfiguration
        if(framework != null){
            framework.shutdown();
        }

        eventStore.close();
        logger.info("Shutdown complete");
    }

    /**
     * Sets the case identifier. Only lowercase alpha-numerics and dashes are supported.
     * @param caseIdentifier New Case Identifier to set.
     */
    private void setCaseIdentifier(String caseIdentifier) {
        // A-Z0-9- and not a reserved keyword
        if (caseIdentifier != null && Pattern.matches("[a-z0-9-]+", caseIdentifier)) {
            this.caseIdentifier = caseIdentifier;
        } else {
            // Generates a default identifier while preventing using an existing one.
            try {
                List<Path> files = new LinkedList<>();
                if(workingDirectory != null && Files.exists(workingDirectory)){
                    files = Files.list(workingDirectory).collect(Collectors.toList());
                }

                int i = 0;
                String counterPrefix = ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
                do{
                    // Testing identifiers: 20170503-1, 20170503-2, and so on.
                    i += 1;
                    this.caseIdentifier = counterPrefix + i;
                }while(files.stream().anyMatch(f -> f.toFile().getName().equalsIgnoreCase(this.caseIdentifier)));
            } catch (IOException e) {
                logger.error("Could not generate a default case identifier.", e);
            }
        }
    }

    /**
     * Gets the case identifier. If not set, one will be generated. The working directory should not be changed if a default case identifier was used.
     * @return Identifier of the current case.
     */
    public String getCaseIdentifier() {
        return caseIdentifier;
    }

    /**
     * Sets the working directory. If an invalid path was provided, it will be reset to default (tmp).
     * @param workingDirectory Path to working directory.
     */
    private void setWorkingDirectory(String workingDirectory){
        try {
            this.workingDirectory = Paths.get(workingDirectory);
        } catch(NullPointerException | IllegalArgumentException e){
            this.workingDirectory = null;
        }
        if (this.workingDirectory == null || this.workingDirectory.getRoot() == null) {
            this.workingDirectory = Paths.get(System.getProperty("java.io.tmpdir"),"maloney");
            logger.debug("Created temporary working directory: {}", this.workingDirectory.toString());
        }
    }

    /**
     * Gets the directory of the current case. If nothing was set, a default is generated.
     * @return Path to the directory.
     */
    public Path getCaseDirectory() {
        try {
            if (caseDirectory == null) {
                caseDirectory = workingDirectory.resolve(getCaseIdentifier());
                Files.createDirectories(caseDirectory);
            }
        } catch (IOException e) {
            logger.error("Could not create temporary working directory.", e);
        }
        return caseDirectory;
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
}