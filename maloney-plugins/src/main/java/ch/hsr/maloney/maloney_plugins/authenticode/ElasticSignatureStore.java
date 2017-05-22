package ch.hsr.maloney.maloney_plugins.authenticode;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by roman on 18.05.17.
 */
public class ElasticSignatureStore implements SignatureStore {
    final ObjectMapper mapper;
    final Logger logger;
    TransportClient client;
    static final String INDEX_NAME = "maloney-signatures";
    static final String SIGNATURE_TYPE = "code-signature";

    /**
     * Creates a new Instance of {@link ElasticSignatureStore} with ElasticSearch as backend.
     *
     * @throws UnknownHostException If the hostname for elasticsearch could not be looked up.
     */
    public ElasticSignatureStore() throws UnknownHostException {
        this.logger = LogManager.getLogger();
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
    }

    /**
     * Updates the data mapping in ES. The index will be created, if it does not exist already.
     *
     * @param force If true, the mapping will be forced to upgrade, whether the index was created or not.
     */
    void updateMapping(boolean force) {
        // https://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html
        boolean wasCreated = false;
        IndicesExistsResponse existsResponse = client.admin().indices().prepareExists(INDEX_NAME).get();
        if (!existsResponse.isExists()) {
            wasCreated = client.admin().indices().prepareCreate(INDEX_NAME).get().isAcknowledged();
        }
        if (wasCreated || force) {
            // Index has to be created to work.
            try {
                XContentBuilder mapping = jsonBuilder()
                        .startObject()
                        .startObject(SIGNATURE_TYPE)
                        .startObject("properties")
                        .startObject("hash")
                        .field("type", "keyword")
                        .endObject() // end hash
                        .startObject("source")
                        .field("type", "keyword")
                        .endObject() // end source
                        .endObject() // end properties
                        .endObject() // end artifact
                        .endObject();

                logger.debug(mapping.string());
                PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping(INDEX_NAME)
                        .setType(SIGNATURE_TYPE)
                        .setSource(mapping).get();
                logger.debug("Update mapping ack? {}", putMappingResponse.isAcknowledged());
            } catch (IOException e) {
                logger.error("Could not update mapping.", e);
            }
        }
    }


    @Override
    public List<String> addSignatures(List<SignatureRecord> records) {
        List<String> addedRecordIds = new LinkedList<>();
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        records.forEach(record -> {
            try {
                byte[] data = mapper.writeValueAsBytes(record);
                bulkRequestBuilder.add(client.prepareIndex(INDEX_NAME, SIGNATURE_TYPE).setSource(data));
            } catch (JsonProcessingException e) {
                logger.error("Could not serialize HashRecord.", e);
            }
        });

        BulkResponse bulkResponse = bulkRequestBuilder.get();
        bulkResponse.forEach(bulkItemResponse -> {
            this.logger.trace("Added document: {} ({})", bulkItemResponse.getId(), bulkItemResponse.getResponse().getResult());
            addedRecordIds.add(bulkItemResponse.getId());
        });
        return addedRecordIds;
    }

    @Override
    public void removeSignature(String id){
        DeleteResponse deleteResponse = client.prepareDelete(INDEX_NAME, SIGNATURE_TYPE, id).get();
        logger.debug("Performed delete action for SignatureRecord with result {}", deleteResponse.status());
    }

    @Override
    public SignatureRecord getSignature(String id){
        GetResponse response = client.prepareGet(INDEX_NAME, SIGNATURE_TYPE, id).get();
        try {
            return mapper.readValue(response.getSourceAsBytes(), SignatureRecord.class);
        } catch (IOException e) {
            logger.error("Could not parse SignatureRecord retrieved from elasticsearch.", e);
        } catch (NullPointerException e) {
            logger.info("Requested SignatureRecord with id '{}' was not found.", id, e);
        }
        return null;
    }

    /**
     * Finds all signatures in the store.
     * @param hash Signature hash to search for. Search is case sensitive.
     * @return List with all matches. If none is found, an empty list is returned.
     */
    @Override
    public List<SignatureRecord> findSignatures(String hash){
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setTypes(SIGNATURE_TYPE)
                .setQuery(QueryBuilders.termQuery("hash", hash)).get();
        if (searchResponse.getHits().getTotalHits() == 0) {
            logger.info("No match found for hash '{}'", hash);
            return new LinkedList<>();
        }
        List<SignatureRecord> records = new LinkedList<>();
        for(SearchHit hit:searchResponse.getHits()){
            try {
                records.add(mapper.readValue(hit.source(), SignatureRecord.class));
            } catch (IOException e) {
                logger.warn("Could not map search result.", e);
            }
        }
        return records;
    }
}
