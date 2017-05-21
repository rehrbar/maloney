package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FakeDataSource;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by roman on 21.05.17.
 */
public class AuthenticodeSignatureLookupJobTest {
    private FakeMetaDataStore fakeMetaDataStore;
    private Context ctx;
    private UUID fileId;

    @Before
    public void prepare() {
        fakeMetaDataStore = new FakeMetaDataStore();
        fileId = UUID.fromString("b6c2364d-163d-432c-b10f-713357c01c92");
        fakeMetaDataStore.addFileAttributes(new FileAttributes(
                "text.exe", "C:\\windows\\",fileId,null, null, null, null,null
        ));
        fakeMetaDataStore.addArtifact(fileId, new Artifact("AuthenticodePEJob", "B207EAA72396B87A82DB095AE73021973BECE60A","authenticode$SHA-1"));
        ctx = new Context(fakeMetaDataStore, null, null);
    }
    @Test
    public void run() throws Exception {
        // TODO provide some way to replace the used store in the job with a fake.
        ElasticSignatureStoreTestImpl store = new ElasticSignatureStoreTestImpl();
        store.clearIndex();
        store.seedTestData();
        store.refreshIndex();

        AuthenticodeSignatureLookupJob job = new AuthenticodeSignatureLookupJob();
        Event evt = new Event("selectedFile", "test", fileId);
        job.run(ctx, evt);

        // Verification
        Artifact result = fakeMetaDataStore.getArtifacts(fileId).stream().filter(artifact -> artifact.getType().equals("authenticode$status")).findFirst().orElse(null);
        assertNotNull(result);
        assertEquals(CertificateStatus.GOOD, result.getValue());
    }

}