package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FakeProgressTracker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by oliver on 29.05.17.
 */
public class ExclusionJobTest {
    private Context ctx;

    private final UUID uuid1 = UUID.randomUUID();
    private final UUID uuid2 = UUID.randomUUID();
    private final UUID uuid3 = UUID.randomUUID();
    private final UUID uuid4 = UUID.randomUUID();

    @Before
    public void setUp(){
        FakeMetaDataStore metaDataStore = new FakeMetaDataStore();
        metaDataStore.addFileAttributes(new FileAttributes("testFile1.exe","/media/someFolder", uuid1, new Date(), new Date(), new Date(), null, null));
        metaDataStore.addFileAttributes(new FileAttributes("testFile2.exe","/media/someOtherFolder", uuid2, new Date(), new Date(), new Date(), null, null));
        metaDataStore.addFileAttributes(new FileAttributes("testFile3.exe","/media/someFolder", uuid3, new Date(), new Date(), new Date(), null, null));
        metaDataStore.addFileAttributes(new FileAttributes("testFile4.exe","/media/someFolder", uuid4, new Date(), new Date(), new Date(), null, null));
        ctx = new Context(metaDataStore, new FakeProgressTracker(), new FakeDataSource());
    }

    @Test
    public void noFilterRun() throws Exception {
        ExclusionJob exclusionJob = new ExclusionJob();
        List<Event> resultingEvents = exclusionJob.run(ctx, new Event("newFile","Test",uuid1));
        Assert.assertTrue(resultingEvents.size() > 0);
    }

    @Test
    public void simpleFilterRun() throws Exception {
        ExclusionJob exclusionJob = new ExclusionJob();
        exclusionJob.setJobConfig("testFile1.exe;");
        //exclusionJob.setJobConfig("FileName:\"testFile1.exe\"");
        List<Event> resultingEvents = exclusionJob.run(ctx, new Event("newFile","Test",uuid1));
        Assert.assertTrue(resultingEvents.size() == 0);
    }

    @Test
    public void multipleFilterRun() throws Exception {
        ExclusionJob exclusionJob = new ExclusionJob();
        exclusionJob.setJobConfig("testFile1.exe;testFile2.exe");
        //TODO switch to pattern after RexEx matcher was added
        //exclusionJob.setJobConfig("FileName:\"testFile1.exe\",FileName:\"testFile2.exe\"");
        List<Event> resultingEvents = exclusionJob.run(ctx, new Event("newFile","Test",uuid1));
        resultingEvents.addAll(exclusionJob.run(ctx, new Event("newFile","Test",uuid2)));
        Assert.assertTrue(resultingEvents.size() == 0);
    }
}