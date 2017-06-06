package ch.hsr.maloney.maloney_plugins.authenticode;

import net.jsign.bouncycastle.cert.X509CertificateHolder;

import java.util.Collection;

public class CertificateVerifier {

    public static CertificateStatus verifyCertificate(X509CertificateHolder main, Collection<X509CertificateHolder> allCerts) {
        // TODO implement the validation of the certificate with the entire chain.
        return CertificateStatus.UNKNOWN;
    }


}