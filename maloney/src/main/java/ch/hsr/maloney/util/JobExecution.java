package ch.hsr.maloney.util;

import ch.hsr.maloney.processing.Job;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by roman on 11.04.17.
 */
public class JobExecution {
    private Job current;
    private Event trigger;
    private List<Event> results;

    public JobExecution(Job job, Event event) {
        this.current = job;
        this.trigger = event;
    }


    public List<Event> getResults() {
        if(results == null){
            results = new LinkedList<>();
        }
        return results;
    }

    public void setResults(List<Event> results) {
        this.results = results;
    }

    public Event getTrigger() {
        return trigger;
    }

    public Job getJob() {
        return current;
    }
}
