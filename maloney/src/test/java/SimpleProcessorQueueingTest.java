import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.SimpleProcessor;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by olive_000 on 28.11.2016.
 */
class FakeJobA implements Job {

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        List<Event> newEvents = new LinkedList<>();
        newEvents.add(new Event("A",this.getJobName(),evt.getFileUuid()));
        return newEvents;
    }

    @Override
    public List<String> getRequiredEvents() {
        return null;
    }

    @Override
    public List<String> getProducedEvents() {
        return null;
    }

    @Override
    public String getJobName() {
        return "FakeJobA";
    }

    @Override
    public String getJobConfig() {
        return null;
    }

    @Override
    public void setJobConfig(String config) {

    }
}

class FakeJobB implements Job{

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        List<Event> newEvents = new LinkedList<>();
        newEvents.add(new Event("B",this.getJobName(),evt.getFileUuid()));
        return newEvents;
    }

    @Override
    public List<String> getRequiredEvents() {
        return null;
    }

    @Override
    public List<String> getProducedEvents() {
        return null;
    }

    @Override
    public String getJobName() {
        return "fakeJobB";
    }

    @Override
    public String getJobConfig() {
        return null;
    }

    @Override
    public void setJobConfig(String config) {

    }
}

public class SimpleProcessorQueueingTest implements Observer {

    private final int NO_OF_EVENTS = 5;
    private int count;
    private SimpleProcessor simpleProcessor;

    private Event randomEvent(String arg){
        return new Event("randomEvent" + arg, "RandomEventGenerator", UUID.randomUUID());
    }

    @Test
    public void oneEventTest(){
        count = 0;
        simpleProcessor = new SimpleProcessor(null);
        Job fakeJobA = new FakeJobA();
        simpleProcessor.addObserver(this);

        for(int i = 0; i < NO_OF_EVENTS; i++){
            simpleProcessor.enqueue(fakeJobA, randomEvent("A"));
        }

        simpleProcessor.start();

        Assert.assertSame(NO_OF_EVENTS*2,count);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            ((List<Event>)arg).forEach(evt -> {
                if(count < NO_OF_EVENTS){
                    simpleProcessor.enqueue(new FakeJobB(), randomEvent("B"));
                }
                count++;
            });
        } catch (ClassCastException e){
            throw new IllegalArgumentException("I just don't know, what to doooooo with this type... \uD83C\uDFB6");
        }
    }
}
