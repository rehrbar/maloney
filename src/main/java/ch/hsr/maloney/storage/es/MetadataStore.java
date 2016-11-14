package ch.hsr.maloney.storage.es;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Logger;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


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
     * @throws UnknownHostException
     */
    public MetadataStore(Logger logger) throws UnknownHostException {
        this.logger = logger;
        // TODO pass configuration to transportclient for cluster name and node.
        // TODO crete index if it does not exist.
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        logger.logInfo("Connected nodes: " + String.join(", ", client.connectedNodes()
                .stream().map(node -> node.getName()).collect(Collectors.toList())));
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    @Override
    public FileAttributes getFileAttributes(UUID fileID) {
        GetResponse response = client.prepareGet(indexName, fileAttributeTypeName, fileID.toString()).get();
        try {
            return mapper.readValue(response.getSourceAsBytes(), FileAttributes.class);
        } catch (IOException e) {
            logger.logError("Could not parse FileAttributes retrieved from elasticsearch.", e);
        }
        return null;
    }

    @Override
    public void addFileAttributes(FileAttributes fileAttributes) {
        // TODO Replace fileId type to generic one: Need to support external file identifier, i.e. sleuthkit file id
        // TODO Provide file identifier as argument.
        try {
            // Mapper would be an easy job, but it cannot handle artifacts as well.
            // TODO Define artifacts as nested type or store them in a separate type.
            // Objects in arrays are not well supported. Relations will be lost.
            byte[] json = mapper.writeValueAsBytes(fileAttributes);
//            XContentBuilder builder = jsonBuilder()
//                    .startObject()
//                    .field("fileName", fileAttributes.getFileName())
//                    .field("filePath", fileAttributes.getFilePath())
//                    .field("fileId", fileAttributes.getFileId())
//                    .field("dateCreated", fileAttributes.getDateCreated())
//                    .field("dateChanged", fileAttributes.getDateChanged())
//                    .field("dateAccessed", fileAttributes.getDateAccessed())
//                    .array("artifacts", fileAttributes.getArtifacts().stream().map(art -> {
//                        try {
//                            return jsonBuilder().startObject()
//                                    .field("originator", art.getOriginator())
//                                    .field("value", art.getValue()) // TODO convert any object to str
//                                    .field("type", art.getType())
//                                    .endObject().string();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }).filter(p -> {System.out.println(p); return p != null;}).toArray())
//                    .endObject();
            IndexResponse response = client.prepareIndex(this.indexName, fileAttributeTypeName, fileAttributes.getFileId().toString())
                    .setSource(json)
                    .get();

            UUID.fromString(response.getId());
        } catch (IOException e) {
            logger.logError("Could not index file attributes into ElasticSearch.", e);
        }
    }

    @Override
    public List<Artifact> getArtifacts(UUID fileId) {
        // TODO Remove this method, because this is not really useful. All Artifacts are sored together as file.
        // See following method call.!
        return this.getFileAttributes(fileId).getArtifacts();
    }

    @Override
    public void addArtifact(UUID fileId, Artifact artifact) {
        // TODO verify if value can be added as object or needs to be converted to json.
        client.prepareUpdate(indexName, fileAttributeTypeName, fileId.toString())
                .setScript(new Script("if (ctx._source.containsKey(\\\"artifacts\\\")) {ctx._source.artifacts += artifact;} else {ctx._source.artifacts = [artifact]}",
                        ScriptService.ScriptType.INLINE,
                        null, new HashMap<String, Artifact>() {{
                    put("artifact", artifact);
                }}))
                .get();
    }

    @Override
    public void addArtifacts(Map<UUID, Artifact> uuidArtifactMap) {
        // TODO This doesn't look like how it's intended to work. Only one artifact per UUID can be added,
        // another artifact with the same UUID replaces the artifact added before.
        throw new UnsupportedOperationException();
    }

}
