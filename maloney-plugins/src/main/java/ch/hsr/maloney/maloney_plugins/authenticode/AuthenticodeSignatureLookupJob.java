package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import ch.hsr.maloney.util.FrameworkEventNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by roman on 21.05.17.
 */
public class AuthenticodeSignatureLookupJob implements Job {
    private static final String JOB_NAME = "AuthenticodeSignatureLookupJob";
    private final Logger logger;
    private SignatureStore signatureStore;

    public AuthenticodeSignatureLookupJob() {
        this(null);
        try {
            signatureStore = new ElasticSignatureStore();
        } catch (UnknownHostException e) {
            logger.error("Could not connect to store.", e);
        }
    }

    AuthenticodeSignatureLookupJob(SignatureStore store){
        logger = LogManager.getLogger();
        signatureStore = store;
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return signatureStore != null;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        List<Event> events = new LinkedList<>();
        Iterator<FileAttributes> iterator = ctx.getMetadataStore().iterator();

        while (iterator.hasNext()) {
            FileAttributes fileAttributes = iterator.next();
            // Matching all artifacts containing hashes
            Collection<Artifact> artifacts = ctx.getMetadataStore().getArtifacts(fileAttributes.getFileId());
            if(artifacts == null){
                logger.info("No artifact available for file {}", fileAttributes.getFileId());
            } else {
                List<Artifact> results = new LinkedList<>();
                for (Artifact a : artifacts) {
                    if (a.getType().startsWith("authenticode")) {
                        List<SignatureRecord> signatures = signatureStore.findSignatures(a.getValue().toString());
                        for (SignatureRecord record : signatures) {
                            results.add(new Artifact(JOB_NAME, record, SignatureRecord.class.getCanonicalName()));
                            results.add(new Artifact(JOB_NAME, record.getStatus(), "authenticode$status"));
                            if(!record.getFileName().equalsIgnoreCase(fileAttributes.getFileName())){
                                // TODO check file name and path?
                                results.add(new Artifact(JOB_NAME, "filename-mismatch", "authenticode$fileName"));
                            }
                        }
                    }
                }
                ctx.getMetadataStore().addArtifacts(fileAttributes.getFileId(), results);

            }
        }
        return events;
    }

    @Override
    public List<String> getRequiredEvents() {
        LinkedList<String> events = new LinkedList<>();
        events.add(FrameworkEventNames.STARTUP);
        return events;
    }

    @Override
    public List<String> getProducedEvents() {
        return new LinkedList<>();
    }

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    public String getJobConfig() {
        return null;
    }

    @Override
    public void setJobConfig(String config) {

    }
}
