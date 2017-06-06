package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.processing.EventNames;
import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import net.jsign.CatalogFile;
import net.jsign.SignedHashInfo;
import net.jsign.bouncycastle.cms.CMSException;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation which examines Authenticode information of a portable executable.
 */
public class AuthenticodeCatalogJob implements Job {
    private static final String JOB_NAME = "AuthenticodeCatalogJob";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;
    private SignatureStore signatureStore;
    private String jobConfig;

    public AuthenticodeCatalogJob() {
        this(null);
    }

    AuthenticodeCatalogJob(SignatureStore store) {
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<>();
        requiredEvents.add(EventNames.ADDED_FILE_EVENT_NAME);
        logger = org.apache.logging.log4j.LogManager.getLogger();
        signatureStore = store;
    }

    private SignatureStore getSignatureStore(Context ctx) {
        if (signatureStore == null) {
            try {
                signatureStore = new ElasticSignatureStore(ctx.getCaseIdentifier());
            } catch (UnknownHostException e) {
                logger.error("Could not connect to signature store.", e);
            }
        }
        return signatureStore;
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        if(ctx.getCaseIdentifier() == null || ctx.getCaseIdentifier().length() < 1){
            logger.info("No case identifier provided. Job will not run.");
            return false;
        }

        // Security Catalog does not contain a magic value like a PE.
        // ASN.1 DER format may often start with 0x30, but it is not guaranteed.
        // Checking the file ending is another good enough guess.
        FileAttributes file = ctx.getMetadataStore().getFileAttributes(evt.getFileUuid());
        return file.getFileName().toLowerCase().endsWith(".cat");
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return getSignatureStore(ctx) != null;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        File eventFile = ctx.getDataSource().getFile(evt.getFileUuid());

        try {
            FileAttributes fileAttributes = ctx.getMetadataStore().getFileAttributes(evt.getFileUuid());

            CatalogFile catalogFile = new CatalogFile(eventFile.getAbsolutePath());

            CertificateStatus certificateStatus = CertificateVerifier.verifyCertificate(catalogFile.getCert(), catalogFile.getCerts());

            List<SignatureRecord> records = new LinkedList<>();
            for (SignedHashInfo hashInfo : catalogFile.getHashInfos()) {
                if (hashInfo.getHashbytes() != null) {
                    String signedFileName = hashInfo.getFilename();
                    if (signedFileName != null) {
                        // File name contains ASCII 0 characters. Removing them helps later in matching.
                        signedFileName = signedFileName.replace("\u0000", "");
                    }

                    SignatureRecord e = new SignatureRecord();
                    e.setFileName(signedFileName);
                    e.setFilePath(fileAttributes.getFilePath());
                    e.setSource(evt.getFileUuid());
                    e.setHash(hashInfo.getHashbytes());
                    e.setStatus(certificateStatus);
                    records.add(e);
                }
            }
            getSignatureStore(ctx).addSignatures(records);
            logger.info("Found {} signatures in {}", records.size(), evt.getFileUuid());

            Path jobWorkingDir = ctx.getDataSource().getJobWorkingDir(AuthenticodeCatalogJob.class);
            UUID certUuid = ctx.getDataSource().addFile(evt.getFileUuid(), new CertificateFileExtractor(jobWorkingDir, evt, catalogFile.getCert(), catalogFile.getCerts()));
            // TODO remove duplicated code
            List<Artifact> certArtifacts = new LinkedList<>();
            certArtifacts.add(new Artifact(JOB_NAME, "authenticode-cert", "filetype"));
            certArtifacts.add(new Artifact(JOB_NAME, certificateStatus, "certificateStatus"));
            ctx.getMetadataStore().addArtifacts(certUuid, certArtifacts);
        } catch (IOException | CMSException e) {
            logger.warn("Security catalog of file {} could not be inspected.", evt.getFileUuid());
        }

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
        return jobConfig;
    }

    @Override
    public void setJobConfig(String config) {
        jobConfig = config;
    }

}
