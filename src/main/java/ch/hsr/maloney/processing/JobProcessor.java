package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Event;
import javafx.beans.Observable;

/**
 * Created by olive_000 on 25.10.2016.
 */
public interface JobProcessor extends Observable {
    /**
     * Starts the Processing Jobs
     */
    void start();

    /**
     * Stops the Processing of Jobs
     */
    void stop();

    /**
     * Adds a Job to be executed with the specified Event.
     *
     * @param job       Job to be executed.
     * @param event     Event which has to be forwarded to Job.
     */
    void enqueue(Job job, Event event);
}
