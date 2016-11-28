package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.hash.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by r1ehrbar on 28.11.2016.
 */
public class ImportRdsHashSetJob implements Job {
    private static final String JOB_NAME = "ImportRdsHashSetJob";
    private final Logger logger;
    private Path zipFile;
    private LinkedList<String> producedEvents;
    private LinkedList<String> requiredEvents;
    private Map<String, String> productList;
    private Map<String, String> osList;
    private HashStore hashStore;
    public static final Pattern FILE_PATTERN = Pattern.compile("\"(?<sha1>[0-9A-F]*)\",\"(?<md5>[0-9A-F]*)\",\"(?<crc32>[0-9A-F]*)\",\"(?<fileName>[^\"]*)\",(?<fileSize>\\d*),(?<productCode>\\d*),\"(?<osCode>[^\"]*)\",\"(?<specialCode>\\w*)\"");


    public ImportRdsHashSetJob() {
        producedEvents = new LinkedList<>();
        // TODO check that this event is generated by the framework.
        requiredEvents = new LinkedList<String>() {{
            push("startup");
        }};
        logger = LogManager.getLogger();
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        if (!zipFile.toFile().exists()) {
            logger.error("RDS file not found at '{}', will not import any hashes.", zipFile.toAbsolutePath().toString());
            return false;
        }
        return true;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        try {
            hashStore = new ElasticHashStore();
        } catch (Exception e) {
            logger.error("Could not connect to hash store. No hashes will be imported.");
            return null;
        }
        try(FileSystem fs = FileSystems.newFileSystem(zipFile, null)){
            Path productList = fs.getPath("/NSRLProd.txt");
            Path osList = fs.getPath("/NSRLOS.txt");
            Path fileList = fs.getPath("/NSRLFile.txt");
            readProductList(productList);
            readOsList(osList);
            readFileList(fileList);
        } catch (IOException e) {
            logger.error("Could not process RDS file.", e);
        }

        // TODO check if null is a possible value, when no events will be thrown.
        return null;
    }

    private void readFileList(Path path) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            sc = new Scanner(Files.newInputStream(path), "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                processHashSetLine(line);
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    private void processHashSetLine(String s) {
        // Skipping header
        if(s.startsWith("\"SHA-1\"")){
            return;
        }
        Matcher matcher = FILE_PATTERN.matcher(s);
        // TODO lookup os and product code.
        try {
            matcher.matches();
            HashRecord record = new HashRecord(HashType.GOOD, zipFile.getFileName().toString(), matcher.group("osCode"), matcher.group("productCode"));
            record.getHashes().put(HashAlgorithm.SHA1, matcher.group("sha1"));
            record.getHashes().put(HashAlgorithm.MD5, matcher.group("md5"));
            record.getHashes().put(HashAlgorithm.CRC32, matcher.group("crc32"));
            hashStore.addHashRecord(record);
        } catch (IllegalStateException | UncheckedIOException e){
            logger.warn("Could not parse line: {}", s);
        }
    }

    private void readOsList(Path path) {
        osList = new HashMap<>();
        // TODO implement when the list is required
    }

    private void readProductList(Path path) {
        productList = new HashMap<>();
        // TODO implement when the list is required.
    }

    @Override
    public List<String> getRequiredEvents() {
        return requiredEvents;
    }

    @Override
    public List<String> getProducedEvents() {
        return producedEvents;
    }

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    public String getJobConfig() {
        return zipFile.toAbsolutePath().toString();
    }

    /**
     * Sets the job configuration
     *
     * @param config Path to the downloaded rds file.
     */
    @Override
    public void setJobConfig(String config) {
        zipFile = Paths.get(config);
    }
}
