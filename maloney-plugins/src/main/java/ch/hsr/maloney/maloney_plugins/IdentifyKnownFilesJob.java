package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.storage.hash.ElasticHashStore;
import ch.hsr.maloney.storage.hash.HashAlgorithm;
import ch.hsr.maloney.storage.hash.HashRecord;
import ch.hsr.maloney.storage.hash.HashStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class IdentifyKnownFilesJob implements Job {
    private static final String JOB_NAME = "IdentifyKnownFilesJob";
    private static final String KNOWN_FILE_EVENT_NAME = "KnownFileFound";
    private static final String MD_5_HASH_TYPE = "MD5Hash"; // TODO define hash types in a common place
    private static final String SHA_1_HASH_TYPE = "SHA1Hash";
    public static final String MD_5_HASH_CALCULATED = "MD5HashCalculated";
    public static final String SHA_1_HASH_CALCULATED = "SHA1HashCalculated";
    private final LinkedList producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;

    public IdentifyKnownFilesJob() {
        producedEvents = new LinkedList<String>() {{
            push(KNOWN_FILE_EVENT_NAME);
        }};
        requiredEvents = new LinkedList<String>() {{
            push(MD_5_HASH_CALCULATED);
            push(SHA_1_HASH_CALCULATED);
        }};
        logger = LogManager.getLogger();
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        // TODO configure HashStore
        HashStore hashStore;
        try {
            hashStore = new ElasticHashStore();
        } catch (Exception e) {
            logger.error("Could not connect to hash store.");
            return null;
        }
        MetadataStore metadataStore = ctx.getMetadataStore();
        List<Artifact> artifacts = metadataStore.getArtifacts(evt.getFileUuid());
        List<Event> events = new LinkedList<>();

        // Get hash and its algorithm which was produced by the event
        String currentHashType = null;
        HashAlgorithm hashAlgorithm = null;
        String hashToSearch = null;
        switch (evt.getName()) {
            case MD_5_HASH_CALCULATED:
                currentHashType = MD_5_HASH_TYPE;
                hashAlgorithm = HashAlgorithm.MD5;
                break;
            case SHA_1_HASH_CALCULATED:
                currentHashType = SHA_1_HASH_TYPE;
                hashAlgorithm = HashAlgorithm.SHA1;
                break;
        }
        for (Artifact artifact : artifacts) {
            if (artifact.getType().equals(currentHashType)) {
                // Simple deserialization of escaped string
                hashToSearch = artifact.getValue().toString().replace("\"", "").toLowerCase();
                logger.debug("Hash found for type {}: {}", currentHashType, hashToSearch);
            }
        }
        if (hashToSearch == null || hashAlgorithm == null) {
            logger.warn("Could not detect the hash to look up. File: {}", evt.getFileUuid());
            return null;
        }

        // Look up hash and add it, if found, as artifact
        HashRecord record = hashStore.findHash(hashToSearch, hashAlgorithm);
        if (record == null) {
            logger.debug("No matching hash found. File {} is unknown.", evt.getFileUuid());
        } else {
            logger.info("Found matching known hash for file UUID '{}'", evt.getFileUuid());
            List<Artifact> newArtifacts = new LinkedList<>();
            newArtifacts.add(new Artifact(getJobName(), record, HashRecord.class.getTypeName()));
            // Additional denormalization to improve performance with Elasticsearch
            newArtifacts.add(new Artifact(getJobName(), record.getType(), HashRecord.class.getTypeName() + "$type"));
            newArtifacts.add(new Artifact(getJobName(), record.getSourceName(), HashRecord.class.getTypeName() + "$sourceName"));
            metadataStore.addArtifacts(evt.getFileUuid(), newArtifacts);
            events.add(new Event(KNOWN_FILE_EVENT_NAME, getJobName(), evt.getFileUuid()));
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
        return "";
    }

    @Override
    public void setJobConfig(String config) {

    }
}
