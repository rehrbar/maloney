package ch.hsr.maloney.maloney_plugins.authenticode;

import java.util.List;

/**
 * Created by roman on 18.05.17.
 */
public interface SignatureStore {
    List<String> addSignatures(List<SignatureRecord> records);

    List<SignatureRecord> findSignatures(String hash);
}
