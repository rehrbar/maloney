package ch.hsr.maloney.storage.hash;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by roman on 27.11.16.
 */
public class ElasticHashStorageTestImpl extends ElasticHashStore {
    /**
     * Creates a new Instance of MetadataStore with ElasticSearch as backend.
     *
     * @throws UnknownHostException
     */
    public ElasticHashStorageTestImpl() throws UnknownHostException {
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
        return client.prepareGet(INDEX_NAME, HASHRECORD_TYPE, id).get().getSourceAsMap();
    }

    public List<String> seedTestData() {
/*

    private HashType type;
    private Map<HashAlgorithm, String> hashes = new HashMap<>();
    private String sourceName;
    private Date updated;
    private String operatingSystem;
    private String productName;
 */
        List<String> generatedIds = new LinkedList<>();
        BulkRequestBuilder bulk = client.prepareBulk();
        try {
            // "0000000F8527DCCAB6642252BBCFA1B8072D33EE","68CE322D8A896B6E4E7E3F18339EC85C","E39149E4","Blended_Coolers_Vanilla_NL.png",30439,28948,"358",""
            bulk.add(client.prepareIndex(INDEX_NAME, HASHRECORD_TYPE)
                    .setSource(jsonBuilder().startObject()
                            .field("type", HashType.GOOD)
                            .field("sourceName", "rds_254u")
                            .field("updated", new Date(1480274695000L))
                            .field("operatingSystem", "TBD")
                            .field("productName", "Caribou Coffee 1.0.7")
                            .startArray("hashes")
                            .startObject()
                            .field(HashAlgorithm.SHA1.toString(), "0000000F8527DCCAB6642252BBCFA1B8072D33EE")
                            .field(HashAlgorithm.MD5.toString(), "68CE322D8A896B6E4E7E3F18339EC85C")
                            .field(HashAlgorithm.CRC32.toString(), "E39149E4")
                            .endObject()
                            .endArray()
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
