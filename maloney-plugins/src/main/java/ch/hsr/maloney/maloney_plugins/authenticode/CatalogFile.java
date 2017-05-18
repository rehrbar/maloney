package ch.hsr.maloney.maloney_plugins.authenticode;

import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.cms.CMSException;
import net.jsign.bouncycastle.cms.CMSProcessable;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.bouncycastle.util.Selector;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by roman on 18.05.17.
 */
public class CatalogFile extends net.jsign.CatalogFile {

    private final CMSSignedData signedData;

    public CatalogFile(byte[] fileContent) throws IOException, CMSException {
        super(fileContent);
        signedData = new CMSSignedData((CMSProcessable) null, ContentInfo.getInstance(fileContent));
    }

    public CatalogFile(String fileName) throws IOException, CMSException {
        this(getContentFromFile(fileName));
    }

    private static byte[] getContentFromFile(String fileName) throws IOException {
        File in = new File(fileName);

        return IOUtils.readFully(new FileInputStream(in), -1, true);
    }

    public Collection<X509CertificateHolder> getCerts() throws CMSException {
        Collection<X509CertificateHolder> certs = new ArrayList<>();
        // TODO can we replace this entire class and implement the correct verification inside Jsign?
        // TODO bring certificates in order to support openssl verification (starting with root, ending with signing certificate).
        signedData.getCertificates().getMatches(new Selector() {
            @Override
            public boolean match(Object o) {
                if(!(o instanceof X509CertificateHolder)) {
                    return false;
                }
                return true;
            }

            @Override
            public Object clone() {
                return null;
            }
        }).forEach(o -> {certs.add((X509CertificateHolder)o);});
        return certs;
    }
}
