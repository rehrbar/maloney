package ch.hsr.maloney.maloney_plugins.authenticode;

import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

/**
 * Created by roman on 18.05.17.
 */
public class SignatureRecord {
    private String hash;
    private String fileName;
    private String filePath;
    private CertificateStatus status;
    private UUID source;

    public SignatureRecord(){
        // keep for serialization
    }

    public SignatureRecord(byte[] hashbytes, String fileName, String filePath, CertificateStatus status, UUID source){
        this.hash = Hex.encodeHexString(hashbytes);
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
        this.source = source;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setHash(byte[] bytes){
        setHash(Hex.encodeHexString(bytes));
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

