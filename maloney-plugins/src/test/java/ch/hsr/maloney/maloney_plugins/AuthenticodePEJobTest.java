package ch.hsr.maloney.maloney_plugins;


import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Created by roman on 15.05.17.
 */
public class AuthenticodePEJobTest {
    private FakeMetaDataStore fakeMetaDataStore;
    private FakeDataSource fakeDataSource;
    private Context ctx;
    @Before
    public void prepare() {
        fakeMetaDataStore = new FakeMetaDataStore();
        fakeDataSource = new FakeDataSource();
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource);
    }
    @Test
    public void IdentifyPE(){
        String path = "/media/sf_shared/PE/7z.exe";
        UUID id = fakeDataSource.addFile(Paths.get(path));
        Job job = new AuthenticodePEJob();
        boolean result = job.shouldRun(ctx, new Event("newFile","test", id));
        Assert.assertTrue("Job should be runnable", result);
    }
}
