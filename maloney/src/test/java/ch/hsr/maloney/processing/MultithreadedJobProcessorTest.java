package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.PlainSource;
import ch.hsr.maloney.storage.SimpleMetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeJobFactory;
import ch.hsr.maloney.util.JobExecution;
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
            if(arg instanceof JobExecution) {
                caughtEvents.addAll(((JobExecution) arg).getResults());
            }
        }
    }

    class QueueingFakeObserver extends FakeObserver{
        private final JobProcessor jobProcessor;
        private final Job queueableJob;

        QueueingFakeObserver(JobProcessor jobProcessor, Job queueableJob){
            this.jobProcessor = jobProcessor;
            this.queueableJob = queueableJob;
        }

        @Override
        public void update(Observable o, Object arg) {
            logger.debug("Fake Observer is adding an Event to its memory");

            if(arg instanceof JobExecution) {
                List<Event> events = ((JobExecution) arg).getResults();
                events.forEach((event) -> {
                    if (queueableJob.getRequiredEvents().contains(event.getName())) {
                        jobProcessor.enqueue(queueableJob, event);
                    }
                });
                caughtEvents.addAll(events);
            }
        }
    }

    Context ctx;
    private final Logger logger = LogManager.getLogger();

    @Before
    public void setUp(){
        SimpleMetadataStore store = new SimpleMetadataStore();
        Context ctx = new Context(store, null, new PlainSource(store));
    }

    @Test
    public void emptyStart(){
        logger.debug("Setting up 'emptyStart'...");
        MultithreadedJobProcessor mtjp = new MultithreadedJobProcessor(ctx);
        mtjp.start();
    }

    @Test(timeout = 1000)
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

        mtjp.waitForFinish();
        logger.debug("Stopped waiting");

        Assert.assertSame(1, fakeObserver.caughtEvents.size());
    }

    private void waitMilliseconds(long timeout) {
        long time = System.currentTimeMillis()+timeout;
        while(time > System.currentTimeMillis()){
            try {
                //logger.debug("Test is waiting...");
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(timeout = 10000)
    public void twoJobsInSequence(){
        logger.debug("Setting up 'twoJobsInSequence'...");
        MultithreadedJobProcessor mtjp = new MultithreadedJobProcessor(ctx);
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        QueueingFakeObserver fakeObserver = new QueueingFakeObserver(mtjp, fakeJobFactory.getAtoBJob());
        mtjp.addObserver(fakeObserver);

        UUID someUuid = UUID.randomUUID();
        mtjp.enqueue(fakeJobFactory.getAJob(()->waitMilliseconds(1000)),new Event("Nothing","Test1", someUuid));
        mtjp.enqueue(fakeJobFactory.getAJob(()->waitMilliseconds(1000)),new Event("Nothing2","Test2", someUuid));

        mtjp.start();
        mtjp.waitForFinish();
        logger.debug("Stopped waiting");
        mtjp.stop();
        Assert.assertEquals(4, fakeObserver.caughtEvents.size());
    }

    @Test(timeout = 10000)
    public void twoStarts(){
        MultithreadedJobProcessor mtjp = new MultithreadedJobProcessor(ctx);
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        QueueingFakeObserver fakeObserverAtoB = new QueueingFakeObserver(mtjp, fakeJobFactory.getAtoBJob(()->waitMilliseconds(250)));
        QueueingFakeObserver fakeObserverBtoC = new QueueingFakeObserver(mtjp, fakeJobFactory.getBtoCJob(()->waitMilliseconds(250)));
        mtjp.addObserver(fakeObserverAtoB);
        mtjp.addObserver(fakeObserverBtoC);

        UUID uuid = UUID.randomUUID();
        JobExecution jobExecution = new JobExecution(
                fakeJobFactory.getAJob(()-> waitMilliseconds(1000)),
                new Event("Start","Test",uuid));
        mtjp.enqueue(jobExecution);

        mtjp.start();
        mtjp.waitForFinish();

       Assert.assertEquals(3,fakeObserverAtoB.caughtEvents.size());

       mtjp.enqueue(jobExecution);
       mtjp.start();
       mtjp.waitForFinish();

       Assert.assertEquals(6,fakeObserverAtoB.caughtEvents.size());
    }
}