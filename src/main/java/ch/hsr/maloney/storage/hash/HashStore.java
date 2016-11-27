package ch.hsr.maloney.storage.hash;

public interface HashStore {
    String addHashRecord(HashRecord record);

    void removeHashRecord(String id);

    HashRecord getHashRecord(String id);

    HashRecord findHash(String hashValue);
    HashRecord findHash(String hashValue, HashType type);
}
