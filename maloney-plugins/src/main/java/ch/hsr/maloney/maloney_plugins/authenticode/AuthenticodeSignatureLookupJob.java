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
    }

    AuthenticodeSignatureLookupJob(SignatureStore store){
        logger = LogManager.getLogger();
        if(store != null) {
            signatureStore = store;
        } else {
            try {
                signatureStore= new ElasticSignatureStore();
            } catch (UnknownHostException e) {
                logger.error("Could not connect to signature store.", e);
            }
        }
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
                    if (a.getType().startsWith("authenticode-hash$")) {
                        String hash = a.getValue().toString();
                        // Artifact value contains quotes at the beginning and the end.
                        // To match properly, they need to be removed.
                        // TODO define artifact value as string for better serialization/deserialization.
                        List<SignatureRecord> signatures = signatureStore.findSignatures(hash.replace("\"",""));
                        for (SignatureRecord record : signatures) {
                            results.add(new Artifact(JOB_NAME, record, SignatureRecord.class.getCanonicalName()));
                            results.add(new Artifact(JOB_NAME, record.getStatus(), "authenticode$status"));

                            // Some catalogs contain file names in their signature.
                            if(record.getFileName() != null && !record.getFileName().equalsIgnoreCase(fileAttributes.getFileName())){
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
