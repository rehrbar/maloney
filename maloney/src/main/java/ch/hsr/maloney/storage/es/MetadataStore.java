package ch.hsr.maloney.storage.es;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * ElasticSearch metadata store.
 */
public class MetadataStore implements ch.hsr.maloney.storage.MetadataStore {
    final ObjectMapper mapper;
    final Logger logger;
    TransportClient client;
    String indexName = "maloney";
    static final String fileAttributeTypeName = "fileAttribute";

    /**
     * Creates a new Instance of MetadataStore with ElasticSearch as backend.
     *
     * @throws UnknownHostException Gets thrown when ElasticSearch instance ip address cannot be resolved
     * @param caseIdentifier
     */
    public MetadataStore(String caseIdentifier) throws UnknownHostException {
        this.logger = LogManager.getLogger();

        // Elasticsearch only works with lower case indices
        this.indexName = "maloney-" + caseIdentifier.toLowerCase();

        // TODO pass configuration to transportclient for cluster name and node.
        Settings settings = Settings.builder()
            .put("cluster.name", "maloney").build();
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        logger.info("Connected nodes: {}", String.join(", ", client.connectedNodes()
                .stream().map(node -> node.getName()).collect(Collectors.toList())));
        updateMapping(false);
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    void updateMapping(boolean force) {
        // https://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html
        boolean wasCreated = false;
        IndicesExistsResponse existsResponse = client.admin().indices().prepareExists(indexName).get();
        if (!existsResponse.isExists()) {
            wasCreated = client.admin().indices().prepareCreate(indexName).get().isAcknowledged();
        }
        if (wasCreated || force) {
            // Index has to be created to work.

            try {
                XContentBuilder mapping = jsonBuilder()
                        .startObject()
                        .startObject(fileAttributeTypeName)
                        .startObject("properties")
                        .startObject("fileId")
                        .field("type", "text")
                        .field("index", "not_analyzed")
                        .endObject() // end fileId
                        .startObject("artifacts")
                        .field("type", "nested")
                        .endObject() // end artifacts
                        .endObject() // end porperties
                        .endObject() // end artifact
                        .endObject();

                logger.debug(mapping.string());
                PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping(indexName)
                        .setType(fileAttributeTypeName)
                        .setSource(mapping).get();
                logger.debug("Update mapping ack? {}", putMappingResponse.isAcknowledged());
            } catch (IOException e) {
                logger.error("Could not update mapping.", e);
            }
        }
    }

    @Override
    public FileAttributes getFileAttributes(UUID fileID) {
        GetResponse response = client.prepareGet(indexName, fileAttributeTypeName, fileID.toString()).get();
        try {
            return mapper.readValue(response.getSourceAsBytes(), FileAttributes.class);
        } catch (IOException e) {
            logger.error("Could not parse FileAttributes retrieved from elasticsearch.", e);
        }
        return null;
    }

    @Override
    public void addFileAttributes(FileAttributes fileAttributes) {
        try {
            XContentBuilder builder = jsonBuilder()
                    .startObject()
                    .field("fileName", fileAttributes.getFileName())
                    .field("filePath", fileAttributes.getFilePath())
                    .field("fileId", fileAttributes.getFileId())
                    .field("dateCreated", fileAttributes.getDateCreated())
                    .field("dateChanged", fileAttributes.getDateChanged())
                    .field("dateAccessed", fileAttributes.getDateAccessed())
                    .endObject();
            client.prepareIndex(this.indexName, fileAttributeTypeName, fileAttributes.getFileId().toString())
                    .setSource(builder)
                    .get();
        } catch (IOException e) {
            logger.error("Could not index file attributes into ElasticSearch.", e);
        }
    }

    @Override
    public Collection<Artifact> getArtifacts(UUID fileId) {
        // TODO Idea: Implement this method correctly with searchers to query for a specific type of an artifact.
        FileAttributes fileAttributes = getFileAttributes(fileId);
        return fileAttributes != null ? fileAttributes.getArtifacts() : null;
    }

    @Override
    public void addArtifact(UUID fileId, Artifact artifact) {
        addArtifacts(fileId, new LinkedList<Artifact>() {{
            push(artifact);
        }});
    }

    @Override
    public void addArtifacts(UUID fileId, Collection<Artifact> artifacts) {
        if (artifacts == null || artifacts.size() == 0) {
            return;
        }
        /* Elasticsearch Script:
            POST maloney/fileAttribute/f99f4262-7b84-440a-b650-ccdd30940511/_update
            {
              "script": {
                "inline":"ctx._source.artifacts == null ? ctx._source.artifacts = [params.artifact] : ctx._source.artifacts.add(params.artifact)",
                "params":{
                  "artifact": {"originator":"test2","value":"ABCDE","type":"base64"}
                }
              }
            }
         */
        BulkRequestBuilder bulk = client.prepareBulk();
        artifacts.forEach(artifact -> {
            try {
                // Script does not support object as parameters, just simple data types.
                // Therefore, newArtifact is defined to wrap the data.
                Script script = new Script("def newArtifact = ['originator':params.originator, 'value':params.value, 'type':params.type]; ctx._source.artifacts == null ? ctx._source.artifacts = [newArtifact] : ctx._source.artifacts.add(newArtifact)",
                        ScriptService.ScriptType.INLINE,
                        null,
                        new HashMap<String, Object>() {{
                            put("originator", artifact.getOriginator());
                            put("value", mapper.writeValueAsString(artifact.getValue()));
                            put("type", artifact.getType());
                        }});
                UpdateRequestBuilder requestBuilder = client.prepareUpdate(indexName, fileAttributeTypeName, fileId.toString())
                        .setScript(script);
                bulk.add(requestBuilder);
            } catch (IOException e) {
                logger.error("Could not serialize artifact for {}", fileId, e);
                return;
            }
        });

        bulk.get();
    }

    @Override
    public Iterator<FileAttributes> iterator() {
        return new ElasticsearchIterator();
    }

    public Iterator<FileAttributes> iterator(int expiryTimeMillis){
        return new ElasticsearchIterator(expiryTimeMillis);
    }

    private class ElasticsearchIterator implements Iterator<FileAttributes> {
        //TODO delete search on index
        final int expiry_time_millis;
        private final int HITS_PER_GET = 100;

        List<FileAttributes> fileAttributes;
        Iterator<FileAttributes> iterator;
        SearchResponse scrollResp;

        ElasticsearchIterator(){
            this(60000);
        }

        ElasticsearchIterator(int expiryTimeMillis){
            this.expiry_time_millis = expiryTimeMillis;
            scrollResp = client.prepareSearch(indexName)
                    .setScroll(new TimeValue(expiry_time_millis))
                    .setSize(HITS_PER_GET).get();
            extractResults();
        }

        private void extractResults() {
            fileAttributes = new LinkedList<>();
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                try {
                    final FileAttributes fileAttribute = mapper.readValue(hit.getSourceAsString(),FileAttributes.class);
                    this.fileAttributes.add(fileAttribute);
                } catch (IOException e) {
                    logger.error("Could not parse FileAttributes retrieved from elasticsearch.", e);
                }
            }
            iterator = fileAttributes.iterator();
        }

        private boolean continueScrolling() {
            scrollResp = client
                    .prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(expiry_time_millis))
                    .execute()
                    .actionGet();
            if(scrollResp.getHits().getHits().length == 0){ // Zero hits mark the end of the scroll
                return false;
            } else {
                extractResults();
                return true;
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext() || continueScrolling();
        }

        @Override
        public FileAttributes next() {
            return iterator.next();
        }
    }

}
//@JsonIgnoreProperties(ignoreUnknown = true)
class ArtifactsDto {
    public List<Artifact> artifacts;
    public ArtifactsDto(){
        // keep for serialization
    }
}
