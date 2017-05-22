package ch.hsr.maloney.maloney_plugins.authenticode;

import java.util.List;

/**
 * Created by roman on 18.05.17.
 */
public interface SignatureStore {

    /**
     * Adds a list of signatures to the store.
     * @param records Signatures to add.
     * @return List with the Identifier of the inserted signatures.
     */
    List<String> addSignatures(List<SignatureRecord> records);

    /**
     * Removes a signature from the store.
     * @param id Identifier of the signature to remove.
     */
    void removeSignature(String id);

    /**
     * Gets a signature by its id.
     * @param id Identifier of the signature.
     * @return The signature record or null, if it does not exists.
     */
    SignatureRecord getSignature(String id);


    /**
     * Finds all signatures in the store.
     * @param hash Signature hash to search for. Search is case sensitive.
     * @return List with all matches. If none is found, an empty list is returned.
     */
    List<SignatureRecord> findSignatures(String hash);
}
