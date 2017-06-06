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
import java.util.*;

/**
 * Implementation which examines Authenticode information of a portable executable.
 */
public class AuthenticodeCatalogJob implements Job {
    private static final String JOB_NAME = "AuthenticodeCatalogJob";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;
    private SignatureStore signatureStore;

    public AuthenticodeCatalogJob() {
        this(null);
    }

    AuthenticodeCatalogJob(SignatureStore store){
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<>();
        requiredEvents.add(EventNames.ADDED_FILE_EVENT_NAME);
        logger = org.apache.logging.log4j.LogManager.getLogger();
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
        // Security Catalog does not contain a magic value like a PE.
        // ASN.1 DER format may often start with 0x30, but it is not guaranteed.
        // Checking the file ending is another good enough guess.
        FileAttributes file = ctx.getMetadataStore().getFileAttributes(evt.getFileUuid());
        return file.getFileName().toLowerCase().endsWith(".cat");
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        return signatureStore != null;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) throws JobCancelledException {
        File eventFile = ctx.getDataSource().getFile(evt.getFileUuid());
        List<Artifact> artifacts = new LinkedList<>();

        try {
            FileAttributes fileAttributes = ctx.getMetadataStore().getFileAttributes(evt.getFileUuid());

            CatalogFile catalogFile = new CatalogFile(eventFile.getAbsolutePath());

            CertificateStatus certificateStatus = CertificateVerifier.verifyCertificate(catalogFile.getCert(), catalogFile.getCerts());

            List<SignatureRecord> records = new LinkedList<>();
            for (SignedHashInfo hashInfo : catalogFile.getHashInfos()) {
                if (hashInfo.getHashbytes() != null) {
                    String signedFileName = hashInfo.getFilename();
                    if(signedFileName != null){
                        // File name contains ASCII 0 characters. Removing them helps later in matching.
                        signedFileName= signedFileName.replace("\u0000","");
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
            signatureStore.addSignatures(records);
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
