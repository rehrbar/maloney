package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation which examines Authenticode information of a portable executable.
 */
public class AuthenticodePEJob implements Job {
    private static final String JOB_NAME = "AuthenticodePEJob";
    private static final String NEW_FILE_EVENT_NAME = "newFile";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;

    public AuthenticodePEJob() {
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<>();
        requiredEvents.add(NEW_FILE_EVENT_NAME);
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        // TODO verify PE file header: MZ
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        return null;
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
        return null;
    }

    @Override
    public void setJobConfig(String config) {

    }
}
