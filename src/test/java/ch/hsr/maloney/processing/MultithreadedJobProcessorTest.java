package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.PlainSource;
import ch.hsr.maloney.storage.SimpleMetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeJobFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by olive_000 on 09.03.2017.
 */
public class MultithreadedJobProcessorTest{
    class FakeObserver implements Observer {
        List<Event> caughtEvents;

        FakeObserver(){
            caughtEvents = new LinkedList<>();
        }

        @Override
        public void update(Observable o, Object arg) {
            caughtEvents.addAll((List<Event>)arg);
        }
    }

    Context ctx;
    private final Logger logger = LogManager.getLogger();

    @Before
    public void setUp(){
        SimpleMetadataStore store = new SimpleMetadataStore();
        Context ctx = new Context(store,null,new PlainSource(store));
    }

    @Test
    public void emptyStart(){
        logger.debug("Setting up 'emptyStart'...");
        MultithreadedJobProcessor mtjp = new MultithreadedJobProcessor(ctx);
        mtjp.start();
    }

    @Test
    public void youHadOneJob(){
        logger.debug("Setting up 'youHadOneJob'...");
        MultithreadedJobProcessor mtjp = new MultithreadedJobProcessor(ctx);
        FakeObserver fakeObserver = new FakeObserver();

        mtjp.addObserver(fakeObserver);

        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        Job job = fakeJobFactory.getAJob();
        UUID someUuid = UUID.randomUUID();

        mtjp.enqueue(job,new Event("Nothing","Test", someUuid));

        logger.debug("Starting MultiThreadedJobProcessor now...");
        mtjp.start();

        waitMilliseconds(500);
        logger.debug("Stopped waiting");

        Assert.assertSame(1, fakeObserver.caughtEvents.size());
    }

    private void waitMilliseconds(long timeout) {;
        long time = System.currentTimeMillis()+timeout;
        while(time > System.currentTimeMillis()){
            try {
                logger.debug("Test is waiting...");
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void twoJobsInSequence(){
        class FakeObserverEnqueuesNext extends FakeObserver{
            private JobProcessor jobProcessor;

            FakeObserverEnqueuesNext(JobProcessor jobProcessor){
                this.jobProcessor = jobProcessor;
            }

            @Override
            public void update(Observable o, Object arg) {
                logger.debug("Fake Observer is adding an Event to its memory");

                List<Event> events = (List<Event>)arg;
                if(events.get(0).getName().equals(FakeJobFactory.eventA)){
                    FakeJobFactory fakeJobFactory = new FakeJobFactory();
                    jobProcessor.enqueue(fakeJobFactory.getAtoBJob(),events.get(0));
                }
                caughtEvents.addAll(events);
            }
        }

        logger.debug("Setting up 'twoJobsInSequence'...");
        MultithreadedJobProcessor mtjp = new MultithreadedJobProcessor(ctx);
        FakeObserverEnqueuesNext fakeObserver = new FakeObserverEnqueuesNext(mtjp);
        mtjp.addObserver(fakeObserver);

        FakeJobFactory fakeJobFactory = new FakeJobFactory();

        UUID someUuid = UUID.randomUUID();
        mtjp.enqueue(fakeJobFactory.getAJob(),new Event("Nothing","Test", someUuid));

        mtjp.start();
        waitMilliseconds(500); //FakeObserver should add Job during waittime
        logger.debug("Stopped waiting");
        mtjp.stop();
        Assert.assertSame(2, fakeObserver.caughtEvents.size());
    }
}