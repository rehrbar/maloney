package ch.hsr.maloney.maloney_plugins;

import ch.hsr.maloney.storage.FileExtractor;
import ch.hsr.maloney.storage.FileSystemMetadata;
import ch.hsr.maloney.util.Event;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collection;

/**
 * Created by roman on 16.05.17.
 */
class CertificateFileExtractor implements FileExtractor {

    private final Path jobWorkingDir;
    private final X509CertificateHolder certificateHolder;
    private Path certPath;
    private final Logger logger;
    private Collection<X509CertificateHolder> certs;

    public CertificateFileExtractor(Path jobWorkingDir, Event evt, X509CertificateHolder certificateHolder) {
        this(jobWorkingDir, evt, certificateHolder, null);
    }

    public CertificateFileExtractor(Path jobWorkingDir, Event evt, X509CertificateHolder certificateHolder, Collection<X509CertificateHolder> certs) {
        this.certs = certs;
        logger = LogManager.getLogger();
        this.jobWorkingDir = jobWorkingDir;
        this.certificateHolder = certificateHolder;
        this.certPath = jobWorkingDir.resolve(evt.getFileUuid().toString() +".crt");
    }

    @Override
    public boolean useOriginalFile() {
        return false;
    }

    @Override
    public Path extractFile() {
        try {
            Files.createDirectories(jobWorkingDir);
            PemWriter pemWriter = new PemWriter(new FileWriter(certPath.toFile()));
            //pemWriter.writeObject(new PemObject("CERTIFICATE", certificateHolder.getEncoded()));
            if(certs != null){
                certs.forEach(o -> {
                    try {
                        pemWriter.writeObject(new PemObject("CERTIFICATE", o.getEncoded()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            logger.error("Could not save embedded certificate file.", e);
        }
        return certPath;
    }

    @Override
    public FileSystemMetadata extractMetadata() {
        File f = certPath.toFile();
        FileSystemMetadata metadata = new FileSystemMetadata();
        metadata.setSize(f.length());
        metadata.setDateAccessed(Calendar.getInstance().getTime());
        // TODO fill additional metadata
        return metadata;
    }

    @Override
    public void cleanup() {
        try {
            Files.deleteIfExists(certPath);
        } catch (IOException e) {
            logger.warn("Could not delete temporary certificate file.", e);
        }
    }
}
