package ch.hsr.maloney.maloney_plugins.authenticode;

import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

/**
 * Created by roman on 18.05.17.
 */
public class SignatureRecord {
    private String hash;
    private String filename;
    private String osAttr;
    private CertificateStatus status;
    private UUID source;

    public SignatureRecord(){
        // keep for serialization
    }

    public SignatureRecord(byte[] hashbytes, String filename, String osAttr, CertificateStatus status, UUID source){
        this.hash = Hex.encodeHexString(hashbytes);
        this.filename = filename;
        this.osAttr = osAttr;
        this.status = status;
        this.source = source;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOsAttr() {
        return osAttr;
    }

    public void setOsAttr(String osAttr) {
        this.osAttr = osAttr;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    public UUID getSource() {
        return source;
    }

    public void setSource(UUID source) {
        this.source = source;
    }
}

