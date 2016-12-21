package ch.hsr.maloney.util;

import java.util.List;

/**
 * Created by olive_000 on 08.11.2016.
 */
public interface ProgressTracker {

    // Running Jobs

    /**
     * @return List of Jobs that are currently running
     */
    List<String> getCurrentJobsRunning();

    /**
     * @param jobsRunning Add to List of Jobs that are currently running
     */
    void addCurrentJobsRunning(List<String> jobsRunning);

    /**
     * @param jobsRunning Remove these from List of currently running Jobs
     */
    void removeCurrentJobsRunning(List<String> jobsRunning);

    // Idling Jobs

    /**
     * @return List of Jobs that are currently idling
     */
    List<String> getCurrentJobsIdling();

    /**
     * @param jobsIdling Add these Jobs to List of currently idling Jobs
     */
    void addCurrentJobsIdling(List<String> jobsIdling);

    /**
     * @param jobsIdling Remove these Jobs from currently idling Jobs
     */
    void removeCurrentJobsIdling(List<String> jobsIdling);

    // Registered Files

    /**
     * @return Number of Files that are registered
     */
    int getNumberOfFiles();

    /**
     * @param numberOfFiles Set Number of registered Files
     */
    void setNumberOfFiles(int numberOfFiles);

    // Registered Directories

    /**
     * @return Number of registered Directories
     */
    int getNumberOfDirectories();

    /**
     * @param numberOfDirectories Number of registered Directories
     */
    void setNumberOfDirectories(int numberOfDirectories);

    // Events Pending

    /**
     * @return Number of pending Events
     */
    int getEventsPending();

    /**
     * @param eventsPending Number of pending Events
     */
    void setEventsPending(int eventsPending);

    // Events Processed

    /**
     * @return Number of processed Events
     */
    int getEventsProcessed();

    /**
     * @param eventsProcessed Number of processed Events
     */
    void setEventsProcessed(int eventsProcessed);
}
