package ch.hsr.maloney.processing;

import ch.hsr.maloney.core.FrameworkEventNames;
import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.storage.FileSystemMetadata;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * A job which can process disk images. Mainly, it adds the image and generates the necessary events for ohter modules.
 */
public class DiskImageJob implements Job {
    private static final String JOB_NAME = "DiskImageJob";
    private static final String NewDiskImageEventName = "newDiskImage";
    private LinkedList<String> producedEvents;
    private LinkedList<String> requiredEvents;
    private Path path;

    public DiskImageJob() {
        producedEvents = new LinkedList<String>() {{
            add(NewDiskImageEventName);
        }};
        requiredEvents = new LinkedList<String>(){{
            add(FrameworkEventNames.STARTUP);
        }};
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        return Files.exists(path);
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        UUID uuid = ctx.getDataSource().addFile(null, new FileExtractor() {

            @Override
            public boolean useOriginalFile() {
                return true;
            }

            @Override
            public Path extractFile() {
                return path;
            }

            @Override
            public FileSystemMetadata extractMetadata() {
                // TODO supply some metadata about the image. E.g. creationDate, name, etc.
                FileSystemMetadata metadata = new FileSystemMetadata();
                metadata.setFileName(path.getFileName().toString());
                return metadata;
            }

            @Override
            public void cleanup() {
                // nothing to cleanup yet
            }
        });

        Event event = new Event(NewDiskImageEventName, JOB_NAME, uuid);
        return new LinkedList<Event>() {{
            add(event);
        }};
    }

    @Override
    public List<String> getRequiredEvents() {
        return requiredEvents;
    }

    @Override
    public List<String> getProducedEvents() {
        return producedEvents;
    }

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    public String getJobConfig() {
        return path.toString();
    }

    @Override
    public void setJobConfig(String config) {
        path = Paths.get(config);
    }
}
