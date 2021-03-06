package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.FrameworkEventNames;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.JobExecution;
import ch.hsr.maloney.util.categorization.Category;
import com.sun.nio.file.ExtendedOpenOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by oliver on 01.05.17.
 */
public class ReportJob implements Job {

    private static final String REPORT_CREATED_EVENT_NAME = "ReportCreated";
    private static final String JOB_NAME = "ReportJob";
    private static final String FILE_NAME = "report.csv";
    private final Logger logger;
    private final char CELL_SEPARATOR = ',';
    private final String CATEGORY_SEPARATOR = " ";

    public ReportJob(){
        logger = LogManager.getLogger();
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        final List<Event> eventList = new LinkedList<>();


        Path targetFile = ctx.getDataSource().getJobWorkingDir(this.getClass()).resolve(FILE_NAME);

        try {
            Files.createDirectories(targetFile.getParent());
        } catch (IOException e) {
            logger.error("Could not create report", e);
            throw new JobCancelledException(new JobExecution(this, evt),"Could not create report", e);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(targetFile)){
            //Write Header
            writer.write("Maloney report,Created on: " + (new Date()).toString() + "\r\n");
            writer.write("File Name,File Path,Date Accessed,Date Changed,Date Created,Categories,Number of Artifacts,Artifacts\r\n");

            final MetadataStore metadataStore = ctx.getMetadataStore();
            final Iterator<FileAttributes> iterator = metadataStore.iterator();

            while(iterator.hasNext()){
                FileAttributes fileAttributes = iterator.next();
                Collection<Artifact> artifacts = fileAttributes.getArtifacts();
                if(artifacts == null){
                    logger.info("No artifact available for file {}", fileAttributes.getFileId());
                    artifacts = new LinkedList<>();
                }

                //Write data to file
                StringBuilder stringBuilder = new StringBuilder();

                //File meta data
                stringBuilder
                        .append(fileAttributes.getFileName()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getFilePath()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getDateAccessed()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getDateChanged()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getDateCreated()).append(CELL_SEPARATOR);

                //Categories
                stringBuilder.append(String.join(CATEGORY_SEPARATOR,
                        ctx.getCategoryService().match(fileAttributes).stream()
                        .map(Category::getName)
                        .collect(Collectors.toSet())));
                stringBuilder.append(CELL_SEPARATOR);

                //Artifacts
                stringBuilder
                        .append(artifacts.size()).append(CELL_SEPARATOR);
                artifacts.forEach(artifact -> stringBuilder
                        .append(artifact.getOriginator()).append(CELL_SEPARATOR)
                        .append(artifact.getType()).append(CELL_SEPARATOR)
                        .append(artifact.getValue()).append(CELL_SEPARATOR));
                stringBuilder.append("\r\n");
                writer.write(stringBuilder.toString());
            }

            logger.info("Created Report at {}", targetFile.toAbsolutePath().toString());
            eventList.add(new Event(REPORT_CREATED_EVENT_NAME, JOB_NAME, evt.getId()));
        } catch (IOException e) {
            logger.error("Failed to create report", e);
        }
        return eventList;
    }

    @Override
    public List<String> getRequiredEvents() {
        List<String> events = new LinkedList<>();
        events.add(FrameworkEventNames.STARTUP);
        return events;
    }

    @Override
    public List<String> getProducedEvents() {
        List<String> events = new LinkedList<>();
        events.add(REPORT_CREATED_EVENT_NAME);
        return events;
    }

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    public String getJobConfig() {
        return null;
    }

    @Override
    public void setJobConfig(String config) {
        //TODO configuration
    }
}
