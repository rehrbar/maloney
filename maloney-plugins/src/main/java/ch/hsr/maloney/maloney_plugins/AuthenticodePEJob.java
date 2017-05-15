package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import net.jsign.DigestAlgorithm;
import net.jsign.PEVerifier;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.pe.PEFile;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation which examines Authenticode information of a portable executable.
 */
public class AuthenticodePEJob implements Job {
    private static final String JOB_NAME = "AuthenticodePEJob";
    private static final String NEW_FILE_EVENT_NAME = "newFile";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;

    public AuthenticodePEJob() {
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<>();
        requiredEvents.add(NEW_FILE_EVENT_NAME);
        logger = org.apache.logging.log4j.LogManager.getLogger();
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        try {
            InputStream is = new FileInputStream(ctx.getDataSource().getFile(evt.getFileUuid()));

            // First two bytes are 4D 5A or 77 90 as integers, also known as MZ
            byte[] buffer = new byte[2];
            int bytesRead = is.read(buffer);
            is.close();
            return bytesRead == 2
                    && buffer[0] == 77
                    && buffer[1] == 90;
        } catch (IOException e) {
            logger.warn("Could not identify portable executable.", e);
        }
        return false;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        File eventFile = ctx.getDataSource().getFile(evt.getFileUuid());
        List<Artifact> artifacts = new LinkedList<>();
        try {
            PEFile pef = new PEFile(eventFile);
            byte[] sha1 = pef.computeDigest(DigestAlgorithm.SHA1);
            byte[] sha256 = pef.computeDigest(DigestAlgorithm.SHA256);
            artifacts.add(new Artifact(JOB_NAME, Hex.encodeHexString(sha1), "authenticode$"+ DigestAlgorithm.SHA1.id));
            artifacts.add(new Artifact(JOB_NAME, Hex.encodeHexString(sha256), "authenticode$"+DigestAlgorithm.SHA256.id));

            PEVerifier verifier = new PEVerifier(pef);
            X509CertificateHolder cert = verifier.getCert();
            if(cert != null){
                artifacts.add(new Artifact(JOB_NAME, cert.getSubject(), "cert$subject"));
            }

            if(verifier.isCorrectlySigned()){
                logger.debug("PE file is correctly signed.");
                artifacts.add(new Artifact(JOB_NAME, "authenticode_ok","signature"));
            } else {
                // No artifact will be added if validation failed. It might still be possible the
                // file is signed by a signature catalog.
                logger.debug("PE file has no valid signature.");
            }

        } catch (IOException e) {
            logger.warn("Could not identify portable executable.", e);
        }
        ctx.getMetadataStore().addArtifacts(evt.getFileUuid(), artifacts);
        return null;
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
