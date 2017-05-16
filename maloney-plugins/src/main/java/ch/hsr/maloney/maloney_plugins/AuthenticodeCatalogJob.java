package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.processing.Job;
import ch.hsr.maloney.processing.JobCancelledException;
import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import net.jsign.CatalogFile;
import net.jsign.SignedHashInfo;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.cms.CMSException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation which examines Authenticode information of a portable executable.
 */
public class AuthenticodeCatalogJob implements Job {
    private static final String JOB_NAME = "AuthenticodeCatalogJob";
    private static final String NEW_FILE_EVENT_NAME = "newFile";
    private final LinkedList<String> producedEvents;
    private final LinkedList<String> requiredEvents;
    private final Logger logger;

    public AuthenticodeCatalogJob() {
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<>();
        requiredEvents.add(NEW_FILE_EVENT_NAME);
        logger = org.apache.logging.log4j.LogManager.getLogger();
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        // Security Catalog does not contain a magic value like a PE.
        // ASN.1 DER format may often start with 0x30, but it is not guaranteed.
        // Checking the file ending is another good enough guess.
        File file = ctx.getDataSource().getFile(evt.getFileUuid());
        return file.getName().endsWith(".cat");
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
            CatalogFile catalogFile = new CatalogFile(eventFile.getAbsolutePath());
            for(SignedHashInfo hashInfo : catalogFile.getHashInfos()){
                if(hashInfo.getHashbytes() != null){
                    // TODO add hashes to a store
                    logger.debug("Do something with the hash infos...{} {}", hashInfo.getFilename(), Hex.encodeHexString(hashInfo.getHashbytes()));
                }
            }

            Path jobWorkingDir = ctx.getDataSource().getJobWorkingDir(AuthenticodeCatalogJob.class);
            try {
                saveCert(jobWorkingDir, "demo", catalogFile.getCert());
                // TODO add cert to store
            } catch (IOException e) {
                logger.error("Could not save embedded certificate file.", e);
            }
        } catch (IOException|CMSException e) {
            logger.warn("Security catalog of file {} could not be inspected.", evt.getFileUuid());
        }

        ctx.getMetadataStore().addArtifacts(evt.getFileUuid(), artifacts);
        return null;
    }

    private Path saveCert(Path jobWorkingDir, String certName, X509CertificateHolder cert) throws IOException {
        Files.createDirectories(jobWorkingDir);
        Path certFileName = jobWorkingDir.resolve(certName+".p7b");
        PemWriter pemWriter = new PemWriter(new FileWriter(certFileName.toFile()));
        pemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
        pemWriter.flush();
        pemWriter.close();
        return certFileName;
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
