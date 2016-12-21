package ch.hsr.maloney.core;

import ch.hsr.maloney.core.Framework;
import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by olive_000 on 22.11.2016.
 */
public class FrameworkTest {
    private final String eventA = "eventA";
    private final String eventB = "eventB";
    private final String eventC = "eventC";

    private class JobA implements Job{

        @Override
        public boolean canRun(Context ctx, Event evt) {
            return true;
        }

        @Override
        public List<Event> run(Context ctx, Event evt) {
            return null;
        }

        @Override
        public List<String> getRequiredEvents() {
            List<String> events = new LinkedList<>();
            return events;
        }

        @Override
        public List<String> getProducedEvents() {
            List<String> events = new LinkedList<>();
            events.add(eventA);
            return events;
        }

        @Override
        public String getJobName() {
            return null;
        }

        @Override
        public String getJobConfig() {
            return null;
        }

        @Override
        public void setJobConfig(String config) {

        }
    }

    private class JobAtoB implements Job{

        @Override
        public boolean canRun(Context ctx, Event evt) {
            return true;
        }

        @Override
        public List<Event> run(Context ctx, Event evt) {
            return null;
        }

        @Override
        public List<String> getRequiredEvents() {
            List<String> events = new LinkedList<>();
            events.add(eventA);
            return events;
        }

        @Override
        public List<String> getProducedEvents() {
            List<String> events = new LinkedList<>();
            events.add(eventB);
            return events;
        }

        @Override
        public String getJobName() {
            return null;
        }

        @Override
        public String getJobConfig() {
            return null;
        }

        @Override
        public void setJobConfig(String config) {

        }
    }

    private class JobBtoC implements Job{

        @Override
        public boolean canRun(Context ctx, Event evt) {
            return true;
        }

        @Override
        public List<Event> run(Context ctx, Event evt) {
            return null;
        }

        @Override
        public List<String> getRequiredEvents() {
            List<String> events = new LinkedList<>();
            events.add(eventB);
            return events;
        }

        @Override
        public List<String> getProducedEvents() {
            List<String> events = new LinkedList<>();
            events.add(eventC);
            return events;
        }

        @Override
        public String getJobName() {
            return null;
        }

        @Override
        public String getJobConfig() {
            return null;
        }

        @Override
        public void setJobConfig(String config) {

        }
    }

    @Test
    public void simpleDependencyTest(){
        Framework framework = new Framework();
        framework.register(new JobA());
        framework.register(new JobAtoB());

        try {
            framework.checkDependencies();
        } catch (Framework.UnrunnableJobException e) {
            fail();
        }
    }

    @Test(expected = Framework.UnrunnableJobException.class)
    public void advancedDependencyTest() throws Framework.UnrunnableJobException{
        Framework framework = new Framework();
        framework.register(new JobA());
        framework.register(new JobBtoC());

        framework.checkDependencies();
        fail();
    }
}
