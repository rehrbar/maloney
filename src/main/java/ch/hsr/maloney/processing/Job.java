package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;

import java.util.List;

/**
 * Created by olive_000 on 01.11.2016.
 */
public interface Job {
    /**
     * Check whether or not the Job can, or should, execute
     *
     * @param ctx Context on which the Job will execute
     * @param evt Event on which the Job is based on
     * @return True if the Job can and should run, false if not
     */
    boolean canRun(Context ctx, Event evt);

    /**
     * Executes this Job
     *
     * @param ctx Context on which the Job will execute
     * @param evt Event on which the Job is based on
     * @return List of Events which created by this Job
     */
    List<Event> run(Context ctx, Event evt);

    /**
     * A List of Events which this Job needs before it can be executed
     *
     * @return List of Events which this Job requires before execution
     */
    List<String> getRequiredEvents();

    /**
     * A List of Events which this Job creates when it is executed
     *
     * @return List of Events which this Job produces after execution
     */
    List<String> getProducedEvents();

    /**
     * Getter for Name of this Job.
     *
     * @return Name of this Job.
     */
    String getJobName();

    /**
     * Getter for Configuration of this Job
     *
     * @return Configuration of this Job
     */
    String getJobConfig();

    /**
     * Setter for Configuration of this job.
     *
     * @param config Configuration, which can be interpreted by the job.
     */
    void setJobConfig(String config);
}
