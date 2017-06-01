package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;

import java.util.*;

/**
 * Created by oliver on 29.05.17.
 */
public class ExclusionJob implements Job {
    private static final String JOB_NAME = "FileExclusionJob";

    private String jobConfig;
    private final Set<String> filter = new HashSet<>();
    private final String CONFIG_SEPARATOR = ";";

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
        FileAttributes fileAttributes = ctx.getMetadataStore().getFileAttributes(evt.getFileUuid());
        List<Event> result = new LinkedList<>();
        if(!filter.contains(fileAttributes.getFileName())){
            result.add(new Event(EventNames.ADDED_FILE_EVENT_NAME,this.getJobName(),evt.getFileUuid()));
        }
        return result;
    }

    @Override
    public List<String> getRequiredEvents() {
        List<String> requiredEvents = new LinkedList<>();
        requiredEvents.add(EventNames.NEW_FILE_EVENT_NAME);
        return requiredEvents;
    }

    @Override
    public List<String> getProducedEvents() {
        List<String> producedEvents = new LinkedList<>();
        producedEvents.add(EventNames.ADDED_FILE_EVENT_NAME);
        return producedEvents;
    }

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    public String getJobConfig() {
        return jobConfig;
    }

    @Override
    public void setJobConfig(String config) {
        jobConfig = config;
        Collections.addAll(filter, config.split(CONFIG_SEPARATOR));
    }
}
