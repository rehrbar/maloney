package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.FrameworkEventNames;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oliver on 01.05.17.
 */
public class ReportJob implements Job {

    private final String reportCreatedEventName = "ReportCreated";
    private final String reportJobName = "ReportJob";
    private final String fileName = "report.csv";
    private final Logger logger;
    private String jobConfig;

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

        final MetadataStore metadataStore = ctx.getMetadataStore();
        final Iterator<FileAttributes> iterator = metadataStore.iterator();

        Path targetFile = ctx.getDataSource().getJobWorkingDir(this.getClass()).resolve(fileName);
        try {
            FileWriter writer = new FileWriter(targetFile.toFile());
            //Write Header
            writer.write("File Name,File Path,Date Accessed,Date Changed,Date Created,Number of Artifacts,Artifacts\r\n");

            while(iterator.hasNext()){
                FileAttributes fileAttributes = iterator.next();
                List<Artifact> artifacts = metadataStore.getArtifacts(fileAttributes.getFileId());

                //TODO decide which events are relevant
                //TODO get relevant types from configuration
                //TODO create registry with previously known and necessary artifact types (known bad etc.)

                //TODO file output (CSV)
                //Write data to file
                StringBuilder stringBuilder = new StringBuilder();
                final char CELL_SEPARATOR = ',';
                stringBuilder
                        .append(fileAttributes.getFileName()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getFilePath()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getDateAccessed()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getDateChanged()).append(CELL_SEPARATOR)
                        .append(fileAttributes.getDateCreated()).append(CELL_SEPARATOR)
                        .append(artifacts.size());
                artifacts.forEach(artifact -> stringBuilder
                        .append(artifact.getOriginator()).append(CELL_SEPARATOR)
                        .append(artifact.getType()).append(CELL_SEPARATOR)
                        .append(artifact.getValue()).append(CELL_SEPARATOR));
                stringBuilder.append("\r\n");
                writer.write(stringBuilder.toString());
            }

            eventList.add(new Event(reportCreatedEventName, reportJobName, evt.getId()));
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
        events.add(reportCreatedEventName);
        return events;
    }

    @Override
    public String getJobName() {
        return reportJobName;
    }

    @Override
    public String getJobConfig() {
        return this.jobConfig;
    }

    @Override
    public void setJobConfig(String config) {
        //TODO configuraiton
    }
}
