package ch.hsr.maloney.maloney_plugins;

import net.jsign.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AuthenticodeHelpers {
    static Path saveCert(Path jobWorkingDir, String certName, X509CertificateHolder cert) throws IOException {
        Files.createDirectories(jobWorkingDir);
        Path certFileName = jobWorkingDir.resolve(certName+".p7b");
        PemWriter pemWriter = new PemWriter(new FileWriter(certFileName.toFile()));
        pemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
        pemWriter.flush();
        pemWriter.close();
        return certFileName;
    }
}
