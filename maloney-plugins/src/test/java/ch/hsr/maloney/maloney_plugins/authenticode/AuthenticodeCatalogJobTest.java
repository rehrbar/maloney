package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.maloney_plugins.authenticode.AuthenticodeCatalogJob;
import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Created by roman on 15.05.17.
 */
public class AuthenticodeCatalogJobTest {
    private FakeMetaDataStore fakeMetaDataStore;
    private FakeDataSource fakeDataSource;
    private Context ctx;

    @Before
    public void prepare() {
        fakeMetaDataStore = new FakeMetaDataStore();
        fakeDataSource = new FakeDataSource();
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource);
    }

//    @After
//    public void cleanup() throws IOException {
//        fakeDataSource.cleanup();
//    }

    @Test
    public void IdentifyPE() {
        String path = "/media/sf_shared/PE/VBoxNetLwf.cat";
        UUID id = fakeDataSource.addFile(Paths.get(path));
        Job job = new AuthenticodeCatalogJob();
        boolean result = job.shouldRun(ctx, new Event("newFile", "test", id));
        Assert.assertTrue("Job should be runnable", result);
    }

    @Test
    public void ValidateCatalogFile() throws JobCancelledException {
        String path = "/media/sf_shared/PE/VBoxNetLwf.cat";
        UUID id = fakeDataSource.addFile(Paths.get(path));
        Job job = new AuthenticodeCatalogJob();
        List<Event> result = job.run(ctx, new Event("newFile", "test", id));
    }
}
