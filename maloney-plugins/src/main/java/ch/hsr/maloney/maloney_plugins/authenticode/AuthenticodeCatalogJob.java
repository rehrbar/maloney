package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import net.jsign.CatalogFile;
import net.jsign.SignedHashInfo;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.cms.CMSException;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation which examines Authenticode information of a portable executable.
 */
public class AuthenticodeCatalogJob implements Job {
    private static final String JOB_NAME = "AuthenticodeCatalogJob";
    private static final String NEW_FILE_EVENT_NAME = "newFile";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;
    private SignatureStore signatureStore;

    public AuthenticodeCatalogJob() {
        this(null);
        try {
            signatureStore= new ElasticSignatureStore();
        } catch (UnknownHostException e) {
            logger.error("Could not connect to signature store.", e);
        }
    }

    AuthenticodeCatalogJob(SignatureStore store){
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<>();
        requiredEvents.add(NEW_FILE_EVENT_NAME);
        logger = org.apache.logging.log4j.LogManager.getLogger();
        signatureStore = store;
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        // Security Catalog does not contain a magic value like a PE.
        // ASN.1 DER format may often start with 0x30, but it is not guaranteed.
        // Checking the file ending is another good enough guess.
        // TODO improve check and fix tests
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
            CatalogFile catalogFile = new CatalogFile(eventFile.getAbsolutePath());

            CertificateStatus certificateStatus = CertificateStatus.BAD; // TODO introduce unknown
            try {
                X509Certificate main = convert(catalogFile.getCert());
                Set<X509Certificate> certs = new HashSet<>();
                for(X509CertificateHolder c: catalogFile.getCerts()){
                    certs.add(convert(c));
                }
                CertificateVerifier.verifyCertificate(main, certs);
                certificateStatus = CertificateStatus.GOOD;
            } catch (CertificateException | NoSuchProviderException e) {
                logger.warn("Could not verify certificate.", e);
            } catch (CertificateVerificationException e) {
                logger.warn("Certificate validation failed.", e);
                certificateStatus = CertificateStatus.BAD;
            }

            List<SignatureRecord> records = new LinkedList<>();
            for (SignedHashInfo hashInfo : catalogFile.getHashInfos()) {
                if (hashInfo.getHashbytes() != null) {
                    SignatureRecord e = new SignatureRecord();
                    e.setFileName(hashInfo.getFilename());
                    e.setSource(evt.getFileUuid());
                    e.setHash(hashInfo.getHashbytes());
                    // TODO verify certificate and set status accordingly.
                    e.setStatus(certificateStatus);
                    records.add(e);
                }
            }
            signatureStore.addSignatures(records);
            logger.info("Found {} signatures in {}", records.size(), evt.getFileUuid());

            Path jobWorkingDir = ctx.getDataSource().getJobWorkingDir(AuthenticodeCatalogJob.class);
            UUID certUuid = ctx.getDataSource().addFile(evt.getFileUuid(), new CertificateFileExtractor(jobWorkingDir, evt, catalogFile.getCert(), catalogFile.getCerts()));
            ctx.getMetadataStore().addArtifact(certUuid, new Artifact(JOB_NAME, "authenticode-cert", "filetype"));
            // TODO also set the status for the certificate itself.
        } catch (IOException | CMSException e) {
            logger.warn("Security catalog of file {} could not be inspected.", evt.getFileUuid());
        }

        ctx.getMetadataStore().addArtifacts(evt.getFileUuid(), artifacts);
        return null;
    }

    private X509Certificate convert(X509CertificateHolder cert) throws CertificateException, NoSuchProviderException, IOException {
        // TODO move to CertificateVerifier
        Security.addProvider(new BouncyCastleProvider());
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509",
                BouncyCastleProvider.PROVIDER_NAME);

        InputStream is = new ByteArrayInputStream(cert.getEncoded());
        return (X509Certificate) certificateFactory.generateCertificate(is);
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
