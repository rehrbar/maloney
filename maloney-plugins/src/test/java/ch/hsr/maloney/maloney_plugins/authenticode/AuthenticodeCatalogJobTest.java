package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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
    private FakeSignatureStore signatureStore;

    @Before
    public void prepare() {
        fakeMetaDataStore = new FakeMetaDataStore();
        fakeDataSource = new FakeDataSource(fakeMetaDataStore);
        ctx = new Context(fakeMetaDataStore, null, fakeDataSource, null);
        ctx.setCaseIdentifier("test");
        signatureStore = new FakeSignatureStore();
    }

    @After
    public void cleanup() throws IOException {
        fakeDataSource.cleanup();
    }

    @Test
    public void IdentifyCatalog() {
        String path = "/media/sf_shared/PE/VBoxNetLwf.cat";
        UUID id = fakeDataSource.addFile(Paths.get(path), null);
        fakeMetaDataStore.addFileAttributes(new FileAttributes("VBoxNetLwf.cat", null, id, null, null, null, null));
        Job job = new AuthenticodeCatalogJob(signatureStore);
        job.setJobConfig("");
        boolean result = job.shouldRun(ctx, new Event("newFile", "test", id));
        Assert.assertTrue("Job should be runnable", result);
    }

    @Test
    public void ValidateCatalogFile() throws JobCancelledException {
        String path = "/media/sf_shared/PE/VBoxNetLwf.cat";
        UUID id = fakeDataSource.addFile(Paths.get(path), null);
        fakeMetaDataStore.addFileAttributes(new FileAttributes("VBoxNetLwf.cat", null, id, null, null, null, null));
        Job job = new AuthenticodeCatalogJob(signatureStore);
        job.setJobConfig("");
        List<Event> result = job.run(ctx, new Event("newFile", "test", id));
        Assert.assertFalse(signatureStore.getAllSignatures().isEmpty());
    }
}
