package ch.hsr.maloney.storage.hash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Hash store based on elastic search.
 */
public class ElasticHashStore implements HashStore {
    final ObjectMapper mapper;
    final Logger logger;
    TransportClient client;
    static final String INDEX_NAME = "hashes";
    static final String HASHRECORD_TYPE = "hashrecord";

    /**
     * Creates a new Instance of MetadataStore with ElasticSearch as backend.
     *
     * @throws UnknownHostException
     */
    public ElasticHashStore() throws UnknownHostException {
        this.logger = LogManager.getLogger();
        // TODO pass configuration to transportclient for cluster name and node.
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        logger.info("Connected nodes: {}", String.join(", ", client.connectedNodes()
                .stream().map(node -> node.getName()).collect(Collectors.toList())));
        updateMapping(false);
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    void updateMapping(boolean force) {
        // https://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html
        boolean wasCreated = false;
        IndicesExistsResponse existsResponse = client.admin().indices().prepareExists(INDEX_NAME).get();
        if (!existsResponse.isExists()) {
            client.admin().indices().prepareCreate(INDEX_NAME).get();
        }
        if (wasCreated || force) {
            // Index has to be created to work.
            try {
                XContentBuilder mapping = jsonBuilder()
                        .startObject()
                        .startObject(HASHRECORD_TYPE)
                        .startObject("properties");
                Arrays.stream(HashAlgorithm.values()).forEach((hashAlgorithm -> {
                    try {
                        mapping.startObject("hashes." + hashAlgorithm)
                                .field("type", "text")
                                .field("index", "not_analyzed")
                                .endObject(); // end hashes
                    } catch (IOException e) {
                        logger.error("Could not prepare mapping for hash ({}).", hashAlgorithm, e);
                    }

                }));
                mapping.endObject() // end properties
                        .endObject() // end artifact
                        .endObject();

                logger.debug(mapping.string());
                PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping(INDEX_NAME)
                        .setType(HASHRECORD_TYPE)
                        .setSource(mapping).get();
                logger.debug("Update mapping ack? {}", putMappingResponse.isAcknowledged());
            } catch (IOException e) {
                logger.error("Could not update mapping.", e);
            }
        }
    }
    @Override
    public String addHashRecord(HashRecord record) {
        try {
            // TODO test if everything is serialized correctly.
            byte[] data = mapper.writeValueAsBytes(record);
            IndexResponse indexResponse = client.prepareIndex(INDEX_NAME, HASHRECORD_TYPE).setSource(data).get();
            logger.debug("Indexed HashRecord with id: {}", indexResponse.getId());
            return indexResponse.getId();
        } catch (JsonProcessingException e) {
            logger.error("Could not serialize HashRecord.", e);
        }
        return null;
    }

    @Override
    public void removeHashRecord(String id) {
        DeleteResponse deleteResponse = client.prepareDelete(INDEX_NAME, HASHRECORD_TYPE, id).get();
        logger.debug("Performed delete action for HashRecord with result {}", deleteResponse.status());
    }

    @Override
    public HashRecord getHashRecord(String id) {
        GetResponse response = client.prepareGet(INDEX_NAME, HASHRECORD_TYPE, id).get();
        try {
            return mapper.readValue(response.getSourceAsBytes(), HashRecord.class);
        } catch (IOException e) {
            logger.error("Could not parse HashRecord retrieved from elasticsearch.", e);
        }
        return null;
    }


    @Override
    public HashRecord findHash(String hashValue) {
        // TODO check if this query is correct.
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setTypes(HASHRECORD_TYPE)
                .setQuery(QueryBuilders.termQuery("hashes.value", hashValue))
                .setFrom(0).setSize(1).get();
            return getHashRecord(searchResponse.getHits().getAt(0).id());
    }

    @Override
    public HashRecord findHash(String hashValue, HashType type) {
        // TODO improve search to pass type to speed up search.
        // TODO make this search term working
    /* Kibana debug console:
GET hashes/hashrecord/_search
{
  "query": {
        "term": {
          "hashes.SHA1":"0000000F8527DCCAB6642252BBCFA1B8072D33EE"
        }
  }
}
     */
        return findHash(hashValue);
    }
}
