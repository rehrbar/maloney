package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FakeMetaDataStore;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FrameworkEventNames;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by roman on 21.05.17.
 */
public class AuthenticodeSignatureLookupJobTest {
    public static final String HASH = "b207eaa72396b87a82db095ae73021973bece60a";
    private FakeMetaDataStore fakeMetaDataStore;
    private Context ctx;
    private UUID file1;
    private UUID file2;
    private FakeSignatureStore store;

    @Before
    public void prepare() throws UnknownHostException {
        fakeMetaDataStore = new FakeMetaDataStore();
        file1 = UUID.fromString("b6c2364d-163d-432c-b10f-713357c01c92");
        file2 = UUID.fromString("30956faf-0f89-4172-a14d-c0fa54277d9b");
        fakeMetaDataStore.addFileAttributes(new FileAttributes(
                "text.exe", "C:\\windows\\", file1,null, null, null, null
        ));
        fakeMetaDataStore.addFileAttributes(new FileAttributes(
                "some.ini", "C:\\windows\\", file2,null, null, null, null
        ));
        fakeMetaDataStore.addArtifact(file1, new Artifact("AuthenticodePEJob", HASH,"authenticode-hash$SHA-1"));
        ctx = new Context(fakeMetaDataStore, null, null, null);
        store = new FakeSignatureStore();
    }
    @Test
    public void run() throws JobCancelledException {
        SignatureRecord record = new SignatureRecord();
        record.setSource(UUID.randomUUID());
        record.setHash(HASH);
        record.setFileName("text.exe");
        record.setFilePath("C:\\windows\\");
        record.setStatus(CertificateStatus.GOOD);
        List<SignatureRecord> records = new LinkedList<>();
        records.add(record);
        store.addSignatures(records);

        Job job = new AuthenticodeSignatureLookupJob(store);

        Event evt = new Event(FrameworkEventNames.STARTUP, "test", null);
        job.run(ctx, evt);

        // Verification
        Artifact result = fakeMetaDataStore.getArtifacts(file1).stream()
                .filter(artifact -> artifact.getOriginator().equals(job.getJobName()) && artifact.getType().equals("authenticode$status"))
                .findFirst().orElse(null);
        assertNotNull(result);
        assertEquals(record.getStatus(), result.getValue());
    }

}