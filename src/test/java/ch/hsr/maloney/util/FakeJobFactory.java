package ch.hsr.maloney.util;

import ch.hsr.maloney.processing.Job;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by olive_000 on 09.03.2017.
 */
public class FakeJobFactory {
    public static final String eventA = "eventA";
    public static final String eventB = "eventB";
    public static final String eventC = "eventC";

    private class FakeJob implements Job {
        private final Logger logger;
        List<String> requiredEvents;
        List<String> createdEvents;
        String jobName;
        String jobConfig;

        FakeJob(List<String> requiresEvent, List<String> producesEvent, String jobName, String jobConfig) {
            if(requiresEvent != null){
                this.requiredEvents = requiresEvent;
            } else {
                requiredEvents = new LinkedList<>();
            }

            if(producesEvent != null){
                this.createdEvents = producesEvent ;
            } else {
                createdEvents = new LinkedList<>();
            }

            this.jobName = jobName;
            this.jobConfig = jobConfig;
            logger = LogManager.getLogger();
        }

        @Override
        public boolean canRun(Context ctx, Event evt) {
            return true;
        }

        @Override
        public List<Event> run(Context ctx, Event evt) {
            List<Event> createdEvents = new LinkedList<>();
            this.createdEvents.forEach((s -> createdEvents.add(new Event(s,jobName, evt.getFileUuid()))));
            logger.debug("Job '{}' will add {} new Events", jobName, createdEvents.size());
            return createdEvents;
        }

        @Override
        public List<String> getRequiredEvents() {
            return requiredEvents;
        }

        @Override
        public List<String> getProducedEvents() {
            return createdEvents;
        }

        @Override
        public String getJobName() {
            return jobName;
        }

        @Override
        public String getJobConfig() {
            return jobConfig;
        }

        @Override
        public void setJobConfig(String config) {
            this.jobConfig = config;
        }
    }

    public Job getAJob(){
        List<String> producedEvents =  new LinkedList<>();
        producedEvents.add(eventA);
        return new FakeJob(null,producedEvents,"JobA","");
    }

    public Job getAtoBJob(){
        List<String> requiredEvents =  new LinkedList<>();
        requiredEvents.add(eventA);
        List<String> producedEvents =  new LinkedList<>();
        producedEvents.add(eventB);
        return new FakeJob(requiredEvents,producedEvents,"JobAtoB","");
    }

    public Job getBtoCJob(){
        List<String> requiredEvents =  new LinkedList<>();
        requiredEvents.add(eventB);
        List<String> producedEvents =  new LinkedList<>();
        producedEvents.add(eventC);
        return new FakeJob(requiredEvents,producedEvents,"JobBtoC","");
    }
}
