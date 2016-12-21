package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Event;

import java.util.Observable;

/**
 * Created by olive_000 on 25.10.2016.
 */
public abstract class JobProcessor extends Observable {
    /**
     * Starts the Processing Jobs
     */
    public abstract void start();

    /**
     * Stops the Processing of Jobs
     */
    public abstract void stop();

    /**
     * Adds a Job to be executed with the specified Event.
     *
     * @param job   Job to be executed.
     * @param event Event which has to be forwarded to Job.
     */
    public abstract void enqueue(Job job, Event event);
}
