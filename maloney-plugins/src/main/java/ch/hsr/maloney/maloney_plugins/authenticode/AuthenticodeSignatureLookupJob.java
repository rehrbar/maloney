package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by roman on 21.05.17.
 */
public class AuthenticodeSignatureLookupJob implements Job {
    private static final String JOB_NAME = "AuthenticodeSignatureLookupJob";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;

    public AuthenticodeSignatureLookupJob() {
        logger = LogManager.getLogger();
        requiredEvents = new LinkedList<>();
        producedEvents = new LinkedList<>();
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        // TODO check if the file has a pe hash value.
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        // TODO perform search for each hash value
        List<Event> events = new LinkedList<>();
        try {
            SignatureStore signatureStore = new ElasticSignatureStore();
            List<Artifact> results = new LinkedList<>();
            List<Artifact> artifacts = ctx.getMetadataStore().getArtifacts(evt.getFileUuid());

            // Matching all artifacts containing hashes
            for(Artifact a : artifacts){
                if(a.getType().startsWith("authenticode")){
                    List<SignatureRecord> signatures = signatureStore.findSignatures(a.getValue().toString());
                    for(SignatureRecord record : signatures){
                        results.add(new Artifact(JOB_NAME, record, SignatureRecord.class.getCanonicalName()));
                        results.add(new Artifact(JOB_NAME, record.getStatus(), "authenticode$status"));
                        // TODO check file name.
                    }
                }
            }
            ctx.getMetadataStore().addArtifacts(evt.getFileUuid(), results);
        } catch (UnknownHostException e) {
            throw new JobCancelledException(null, "Could not connect to store.", e);
        }
        return events;
    }

    @Override
    public List<String> getRequiredEvents() {
        return requiredEvents;
    }

    @Override
    public List<String> getProducedEvents() {
        return producedEvents;
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
