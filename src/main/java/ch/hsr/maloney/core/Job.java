package ch.hsr.maloney.core;

import java.util.List;

/**
 * Created by olive_000 on 01.11.2016.
 */
public interface Job {
    boolean canRun(Context ctx, Event evt);
    List<Event> run(Context ctx, Event evt);
    List<Event> getRequiredEvents();
    List<Event> getProducedEvents();
    String getJobName();
    String getJobConfig();
}
