package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author oniet
 *
 * Calcultes MD5 and SHA-1 Hashes
 */
public class CalculateHashesJob implements Job {
    private static final String JOB_NAME = "CalculateHashesJob";
    private static final String MD_5_HASH_TYPE = "MD5Hash";
    private static final String SHA_1_HASH_TYPE = "SHA1Hash";

    private static final int BUFFER_SIZE = 1024;

    private final List<String> requiredEvents = new LinkedList<>();
    private final List<String> producedEvents = new LinkedList<>();
    private final Logger logger;

    public CalculateHashesJob() {
        logger = LogManager.getLogger();
        requiredEvents.add(EventNames.ADDED_FILE_EVENT_NAME);
        producedEvents.add(EventNames.MD_5_HASH_CALCULATED_EVENT_NAME);
        producedEvents.add(EventNames.SHA_1_HASH_CALCULATED_EVENT_NAME);
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        logger.debug("Calculating Hash for file UUID '{}'", evt.getFileUuid());
        MetadataStore metadataStore = ctx.getMetadataStore();
        DataSource dataSource = ctx.getDataSource();
        UUID fileUuid = evt.getFileUuid();

        MessageDigest md5;
        MessageDigest sha1;

        byte[] md5digest;
        byte[] sha1digest;

        List<Event> events = new LinkedList<>();
        InputStream is = null;

        try {
            md5 = MessageDigest.getInstance("MD5");
            sha1 = MessageDigest.getInstance("SHA-1");

            is = dataSource.getFileStream(fileUuid);

            DigestInputStream md5dis = new DigestInputStream(is, md5);
            DigestInputStream sha1dis = new DigestInputStream(md5dis, sha1);

            byte[] buffer = new byte[BUFFER_SIZE];
            while(sha1dis.read(buffer) != -1);

            logger.debug("Done reading file '{}', now encoding and transfering to MetadataStore", fileUuid);
            md5digest = md5.digest();
            sha1digest = sha1.digest();

            // Convert from Byte Digest to String
            String md5hash = new String(Hex.encodeHex(md5digest));
            String sha1hash = new String(Hex.encodeHex(sha1digest));

            List<Artifact> artifacts = new LinkedList<>();
            artifacts.add(new Artifact(getJobName(), md5hash, MD_5_HASH_TYPE));
            artifacts.add(new Artifact(getJobName(), sha1hash, SHA_1_HASH_TYPE));

            metadataStore.addArtifacts(fileUuid, artifacts);

            events.add(new Event(EventNames.MD_5_HASH_CALCULATED_EVENT_NAME, getJobName(), fileUuid));
            events.add(new Event(EventNames.SHA_1_HASH_CALCULATED_EVENT_NAME, getJobName(), fileUuid));

        } catch (IOException e) {
            logger.error("Could not read file with UUID: " + fileUuid.toString(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not find specified Algorithm to calculate Hash", e);
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Could not close InputStream", e);
                }
            }
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
        //TODO not necessary as of yet
    }

    @Override
    public void setJobConfig(String config) {
        // no configuration intended yet.
    }
}
