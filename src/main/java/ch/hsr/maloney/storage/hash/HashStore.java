package ch.hsr.maloney.storage.hash;

/**
 * Interface for a store which can manage hashes and perform lookups.
 */
public interface HashStore {
    /**
     * Adds a new record to the store.
     *
     * @param record Record to add.
     * @return Generated id of the record in the store.
     */
    String addHashRecord(HashRecord record);

    /**
     * Removes a record from the store.
     *
     * @param id Identifier of the record to delete.
     */
    void removeHashRecord(String id);

    /**
     * Gets a specific record.
     *
     * @param id Identifier of the record to retrieve.
     * @return  Specified record.
     */
    HashRecord getHashRecord(String id);

    /**
     * Finds a record in the store based on the hash string.
     * This lookup might take longer because all algorithms need to be checked.
     *
     * @param hashValue Hash string to look up.
     * @return Matched record or null, if it was not found.
     */
    HashRecord findHash(String hashValue);

    /**
     * Finds a record in the store based on the hash string.
     *
     * @param hashValue Hash string to look up.
     * @param algorithm Hash algorithm to narrow the search.
     * @return Matched record or null, if it was not found.
     */
    HashRecord findHash(String hashValue, HashAlgorithm algorithm);
}
