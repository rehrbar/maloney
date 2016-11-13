package ch.hsr.maloney.storage.es;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MetadataStoreTestImpl extends ch.hsr.maloney.storage.es.MetadataStore {
    public MetadataStoreTestImpl() throws UnknownHostException {
        super();
    }

    public void clearIndex() {
        try {
            System.out.println("Deleting index...");
            client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();

        } catch (Exception e) {
            System.out.println("Could not delete index.");
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
        bulk.add(client.prepareIndex(indexName, fileAttributeTypeName, "2db50b31-8927-4833-8bb1-0ec9150c12c3")
                .setSource(
                        "fileName", "notepad.exe",
                        "filePath", "C:\\Windows\\",
                        "fileId", "1337",
                        "dateChanged", new Date(1436471820000L),
                        "dateCreated", new Date(1473823035000L),
                        "dateAccessed", new Date(1473823035000L)
                )
        );
        bulk.add(client.prepareIndex(indexName, fileAttributeTypeName, "f99f4262-7b84-440a-b650-ccdd30940511")
                .setSource(
                        "fileName", "cmd.exe",
                        "filePath", "C:\\Windows\\",
                        "fileId", "5544",
                        "dateChanged", new Date(1436471820000L),
                        "dateCreated", new Date(1473823035000L),
                        "dateAccessed", new Date(1473823035000L)
                )
        );
        bulk.add(client.prepareIndex(indexName, fileAttributeTypeName, "683249b6-a3e1-41e2-bda5-74c6eab36681")
                .setSource(
                        "fileName", "java.exe",
                        "filePath", "C:\\Program Files\\Java\\jre1.8.0_92\\bin",
                        "fileId", "1338",
                        "dateChanged", new Date(1473440417000L),
                        "dateCreated", new Date(1473440417000L),
                        "dateAccessed", new Date(1473440417000L)
                )
        );
        BulkResponse bulkResponse = bulk.get();
        bulkResponse.forEach(bulkItemResponse -> {
            System.out.println(String.format("-> Added document: %s (%s)", bulkItemResponse.getId(), bulkItemResponse.getResponse().getResult()));
            generatedIds.add(bulkItemResponse.getId());
        });
        return generatedIds;
    }
}
