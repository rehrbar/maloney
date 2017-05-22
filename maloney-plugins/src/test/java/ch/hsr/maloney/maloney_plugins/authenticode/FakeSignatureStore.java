package ch.hsr.maloney.maloney_plugins.authenticode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fake implementation of {@see SignatureStore}.
 */
public class FakeSignatureStore implements SignatureStore {
    private Map<String, SignatureRecord> store = new HashMap<>();

    @Override
    public List<String> addSignatures(List<SignatureRecord> records) {
        return records.stream().map(r -> {
            String k = UUID.randomUUID().toString();
            store.put(k, r);
            return k;
        }).collect(Collectors.toList());
    }

    @Override
    public void removeSignature(String id) {
        store.remove(id);
    }

    @Override
    public SignatureRecord getSignature(String id) {
        return store.get(id);
    }

    @Override
    public List<SignatureRecord> findSignatures(String hash) {
        return store.values().stream().filter(r -> r.getHash().equals(hash)).collect(Collectors.toList());
    }

    public List<SignatureRecord> getAllSignatures(){
        List<SignatureRecord> signatureRecords = new LinkedList<>();
        signatureRecords.addAll(store.values());
        return signatureRecords;
    }
}
