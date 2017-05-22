package ch.hsr.maloney.maloney_plugins.authenticode;

import ch.hsr.maloney.storage.hash.HashAlgorithm;
import ch.hsr.maloney.storage.hash.HashType;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by roman on 18.05.17.
 */
public class ElasticSignatureStoreTestImpl extends ElasticSignatureStore {
    /**
     * Creates a new Instance of {@link ElasticSignatureStore} with ElasticSearch as backend.
     *
     * @throws UnknownHostException If the hostname for elasticsearch could not be looked up.
     */
    public ElasticSignatureStoreTestImpl() throws UnknownHostException {
        super();
    }


    public void clearIndex() {
        try {
            logger.info("Deleting index...");
            client.admin().indices().delete(new DeleteIndexRequest(INDEX_NAME)).actionGet();
            updateMapping(true);
        } catch (Exception e) {
            logger.warn("Could not delete index.", e);
        }
    }

    public void refreshIndex() {
        // ATTENTION! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //   This refresh is required so that searches will perform correctly in the index.
        //   More info about refresh and flush see http://stackoverflow.com/a/19973721
        // ATTENTION! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
    }

    public Map<String, Object> dumpRecord(String id){
        return client.prepareGet(INDEX_NAME, SIGNATURE_TYPE, id).get().getSourceAsMap();
    }

    public List<String> seedTestData() {
        List<String> generatedIds = new LinkedList<>();
        BulkRequestBuilder bulk = client.prepareBulk();
        try {
            bulk.add(client.prepareIndex(INDEX_NAME, SIGNATURE_TYPE)
                    .setSource(jsonBuilder().startObject()
                            .field("hash", "b207eaa72396b87a82db095ae73021973bece60a")
                            .field("filename", "vboxnetlwf.sys")
                            .field("osAttr", "2:5.1,2:5.2,2:6.0")
                            .field("status", "GOOD")
                            .field("source", UUID.fromString("75d856e0-b439-4823-97cf-c3e9b09764e9"))
                            .endObject()
                    )
            );
            bulk.add(client.prepareIndex(INDEX_NAME, SIGNATURE_TYPE)
                    .setSource(jsonBuilder().startObject()
                            .field("hash", "abc9556efe149dd31b171e84ce1c3974f49aac49")
                            .field("filename", "vboxnetlwf.inf")
                            .field("osAttr", "2:5.1,2:5.2,2:6.0")
                            .field("status", "BAD")
                            .field("source", UUID.fromString("75d856e0-b439-4823-97cf-c3e9b09764e9"))
                            .endObject()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        BulkResponse bulkResponse = bulk.get();
        bulkResponse.forEach(bulkItemResponse -> {
            this.logger.info("-> Added document: {} ({})", bulkItemResponse.getId(), bulkItemResponse.getResponse().getResult());
            generatedIds.add(bulkItemResponse.getId());
        });
        return generatedIds;
    }
}
