package ch.hsr.maloney.storage.hash;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HashRecord {
    private HashType type;
    private Map<HashAlgorithm, String> hashes = new HashMap<>();
    private String sourceName;
    private Date updated;
    private String operatingSystem;
    private String productName;

    public HashRecord(){
        // Keep constructor for serialization.
    }

    public HashRecord(HashType type, String sourceName, String os, String product){
        this.type = type;
        this.sourceName = sourceName;
        this.operatingSystem = os;
        this.productName = product;
    }

    public HashType getType() {
        return type;
    }

    public void setType(HashType type) {
        this.type = type;
    }

    public Map<HashAlgorithm, String> getHashes() {
        return hashes;
    }

    public void setHashes(Map<HashAlgorithm, String> hashes) {
        this.hashes = hashes;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
