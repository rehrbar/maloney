package ch.hsr.maloney.storage.es;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class MetadataStoreTestImpl extends ch.hsr.maloney.storage.es.MetadataStore {
    public MetadataStoreTestImpl() throws UnknownHostException {
        super("TEST");
    }

    public void clearIndex() {
        try {
            logger.info("Deleting index...");
            client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
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
        client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }

    public List<String> seedTestData() {
        List<String> generatedIds = new LinkedList<>();
        BulkRequestBuilder bulk = client.prepareBulk();
        try {
            bulk.add(client.prepareIndex(indexName, fileAttributeTypeName, "2db50b31-8927-4833-8bb1-0ec9150c12c3")
                    .setSource(jsonBuilder().startObject()
                            .field("fileName", "notepad.exe")
                            .field("filePath", "C:\\Windows\\")
                            .field("fileId", "2db50b31-8927-4833-8bb1-0ec9150c12c3")
                            .field("dateChanged", new Date(1436471820000L))
                            .field("dateCreated", new Date(1473823035000L))
                            .field("dateAccessed", new Date(1473823035000L))
                            .field("parentId", "dadec7c6-ad8c-4f80-b6da-379fceccd0fc")
                            .startArray("artifacts")
                            .startObject()
                            .field("type", "base")
                            .field("value", "bm90ZXBhZC5leGU=")
                            .field("originator", "test")
                            .endObject()
                            .endArray()
                            .endObject()
                    )
            );
            bulk.add(client.prepareIndex(indexName, fileAttributeTypeName, "f99f4262-7b84-440a-b650-ccdd30940511")
                    .setSource(
                            "fileName", "cmd.exe",
                            "filePath", "C:\\Windows\\",
                            "fileId", "f99f4262-7b84-440a-b650-ccdd30940511",
                            "dateChanged", new Date(1436471820000L),
                            "dateCreated", new Date(1473823035000L),
                            "dateAccessed", new Date(1473823035000L),
                            "parentId", "dadec7c6-ad8c-4f80-b6da-379fceccd0fc"
                    )
            );
            bulk.add(client.prepareIndex(indexName, fileAttributeTypeName, "683249b6-a3e1-41e2-bda5-74c6eab36681")
                    .setSource(
                            "fileName", "java.exe",
                            "filePath", "C:\\Program Files\\Java\\jre1.8.0_92\\bin",
                            "fileId", "683249b6-a3e1-41e2-bda5-74c6eab36681",
                            "dateChanged", new Date(1473440417000L),
                            "dateCreated", new Date(1473440417000L),
                            "dateAccessed", new Date(1473440417000L),
                            "parentId", "dadec7c6-ad8c-4f80-b6da-379fceccd0fc"
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

    public String dumpFileAttributeSource(UUID id) {
        return client.prepareGet(indexName, fileAttributeTypeName, id.toString()).get().getSourceAsString();
    }
}
