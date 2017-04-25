package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeJobFactory;
import ch.hsr.maloney.util.JobExecution;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class SimpleProcessorQueueingTest implements Observer {

    private final int NO_OF_EVENTS = 5;
    private int count;
    private SimpleProcessor simpleProcessor;
    private FakeJobFactory jobFactory = new FakeJobFactory();

    private Event randomEvent(String arg){
        return new Event("randomEvent" + arg, "RandomEventGenerator", UUID.randomUUID());
    }

    @Test
    public void oneEventTest(){
        count = 0;
        simpleProcessor = new SimpleProcessor(null);
        Job fakeJobA = jobFactory.getAJob();
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
            ((JobExecution)arg).getResults().forEach(evt -> {
                if(evt.getName() == FakeJobFactory.eventA){
                    simpleProcessor.enqueue(jobFactory.getAtoBJob(), randomEvent("B"));
                }
                count++;
            });
        } catch (ClassCastException e){
            throw new IllegalArgumentException("I just don't know, what to doooooo with this type... \uD83C\uDFB6");
        }
    }
}
