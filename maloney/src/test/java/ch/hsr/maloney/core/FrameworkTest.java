package ch.hsr.maloney.core;

import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeJobFactory;
import ch.hsr.maloney.util.JobExecution;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Observable;
import java.util.UUID;

import static org.junit.Assert.fail;

/**
 * Created by olive_000 on 22.11.2016.
 */
public class FrameworkTest {

    private class FakeFramework extends Framework{
        @Override
        protected void initializeContext() {
            // TODO replace with parameters passed by framework controller
            // TODO create fake progress tracker and add it
            this.context = new Context(new FakeMetaDataStore(), null, new FakeDataSource());
        }
    }

    @Test
    public void simpleDependencyTest(){
        Framework framework = new FakeFramework();
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        framework.register(fakeJobFactory.getAJob());
        framework.register(fakeJobFactory.getAtoBJob());

        try {
            framework.checkDependencies();
        } catch (Framework.UnrunnableJobException e) {
            fail();
        }
    }

    @Test(expected = Framework.UnrunnableJobException.class)
    public void advancedDependencyTest() throws Framework.UnrunnableJobException{
        Framework framework = new FakeFramework();
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        framework.register(fakeJobFactory.getAJob());
        framework.register(fakeJobFactory.getBtoCJob());

        framework.checkDependencies();
        fail();
    }

    /**
     * Test to check if the limit determined by findMaxEventsTest can be still reached.
     */
    @Test
    public void maxEventsTest(){
        setLogVerbosity();

        // actual test
        int increment = 11000000;

        LinkedList<Event> events = new LinkedList<>();
        UUID fileUuid = UUID.randomUUID();
        for(int j = 0; j < increment; j++){
            events.add(new Event(FakeJobFactory.eventA,"Test", fileUuid));
        }

        Framework framework = new FakeFramework();
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        framework.register(fakeJobFactory.getAJob());
        framework.register(fakeJobFactory.getAtoBJob());
        JobExecution jobExecution = new JobExecution(null, null);
        jobExecution.setResults(events);
        framework.update(new Observable(), jobExecution);
        framework.start();
    }

    /**
     * Test to determine max events.
     */
//    @Test
//    public void findMaxEventsTest(){
//        setLogVerbosity();
//
//        // actual test
//        int increment = 1000000;
//        int upperBound = Integer.MAX_VALUE - increment;
//
//        LinkedList<Event> events = new LinkedList<>();
//
//        for(int i = increment; i < upperBound; i+= increment) {
//            System.out.printf("Current iteration: %d\n", i);
//            Framework framework = new FakeFramework();
//            FakeJobFactory fakeJobFactory = new FakeJobFactory();
//            framework.register(fakeJobFactory.getAJob());
//            framework.register(fakeJobFactory.getAtoBJob());
//            for(int j = 0; j < increment; j++){
//                events.add(new Event(fakeJobFactory.eventA,"Test", UUID.randomUUID()));
//            }
//            framework.update(new Observable(), events);
//            framework.start();
//            System.out.println("Finished");
//        }
//    }

    private void setLogVerbosity() {
        // setting log level
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.INFO);
        ctx.updateLoggers();
    }
}
