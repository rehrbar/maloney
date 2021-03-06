package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.FrameworkEventNames;
import ch.hsr.maloney.storage.hash.*;
import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A job to import a zipped National Institute of Standards and Technology (NIST)
 * National Software Reference Library (NSRL) Reference Data Set (RDS).
 * This implementation supports the reduced unique set available at <a href="http://www.nsrl.nist.gov/Downloads.htm">NSRL Downloads</a>.
 */
public class ImportRdsHashSetJob implements Job {
    private static final String JOB_NAME = "ImportRdsHashSetJob";
    private static final int BUFFER_LIMIT = 10000;
    private final Logger logger;
    private Path zipFile;
    private LinkedList<String> producedEvents;
    private LinkedList<String> requiredEvents;
    private Map<String, String> productList;
    private Map<String, String> osList;
    private HashStore hashStore;
    public static final Pattern FILE_PATTERN = Pattern.compile("\"(?<sha1>[0-9A-F]*)\",\"(?<md5>[0-9A-F]*)\",\"(?<crc32>[0-9A-F]*)\",\"(?<fileName>[^\"]*)\",(?<fileSize>\\d*),(?<productCode>\\d*),\"(?<osCode>[^\"]*)\",\"(?<specialCode>\\w*)\"");
    public static final Pattern OS_PATTERN = Pattern.compile("\"(?<osCode>[^\"]*)\",\"(?<osName>[^\"]*)\",\"(?<osVersion>[^\"]*)\",\"(?<mfgCode>[^\"]*)\"");
    public static final Pattern PRODUCT_PATTERN = Pattern.compile("(?<productCode>[\\d]*),\"(?<productName>[^\"]*)\",\"(?<productVersion>[^\"]*)\",\"(?<osCode>[^\"]*)\",\"(?<mfgCode>[^\"]*)\",\"(?<language>[^\"]*)\",\"(?<applicationType>[^\"]*)\"");


    public ImportRdsHashSetJob() {
        producedEvents = new LinkedList<>();
        requiredEvents = new LinkedList<String>() {{
            push(FrameworkEventNames.STARTUP);
        }};
        logger = LogManager.getLogger();
    }

    @Override
    public boolean shouldRun(Context ctx, Event evt) {
        // TODO verify correct implementation of this shouldRun
        return zipFile != null && zipFile.getRoot() != null;
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        if (zipFile == null || !zipFile.toFile().exists()) {
            logger.error("RDS file not found at '{}', will not import any hashes.", zipFile == null ? "n/a" : zipFile.toAbsolutePath().toString());
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
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, null)) {
            Path productList = fs.getPath("/NSRLProd.txt");
            Path osList = fs.getPath("/NSRLOS.txt");
            Path fileList = fs.getPath("/NSRLFile.txt");
            readProductList(productList);
            readOsList(osList);
            readFileList(fileList);
        } catch (IOException e) {
            logger.error("Could not process RDS file.", e);
        }

        return null;
    }

    private void readFileList(Path path) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        List<HashRecord> buffer = new LinkedList<>();
        try {
            // File is encoded in ASCII, but fileNames can be encoded different.
            // Using UTF-8 encoding may cause nextLine to break in this field, resulting in not added hashes.
            sc = new Scanner(Files.newInputStream(path), "US-ASCII");

            // Skipping header
            sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                processHashSetLine(line, buffer);

                // Flush buffer
                if (buffer.size() >= BUFFER_LIMIT) {
                    ((ElasticHashStore) hashStore).addHashRecords(buffer);
                    buffer.clear();
                }
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

        // Flush leftovers.
        if (buffer.size() > 0) {
            ((ElasticHashStore) hashStore).addHashRecords(buffer);
        }
    }

    private void processHashSetLine(String s, List<HashRecord> buffer) {
        Matcher matcher = FILE_PATTERN.matcher(s);
        try {
            matcher.matches();
            String os = osList.get(matcher.group("osCode")); // TODO default values?
            String product = productList.get(matcher.group("productCode"));
            HashRecord record = new HashRecord(HashType.GOOD, zipFile.getFileName().toString(), os, product);
            record.getHashes().put(HashAlgorithm.SHA1, matcher.group("sha1").toLowerCase());
            record.getHashes().put(HashAlgorithm.MD5, matcher.group("md5").toLowerCase());
            record.getHashes().put(HashAlgorithm.CRC32, matcher.group("crc32").toLowerCase());
            buffer.add(record);
        } catch (IllegalStateException | UncheckedIOException e) {
            logger.warn("Could not parse line: {}", s);
        }
    }

    private void readOsList(Path path) throws IOException {
        osList = new HashMap<>();
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            // File is encoded in ASCII, but fileNames can be encoded different.
            // Using UTF-8 encoding may cause nextLine to break in this field, resulting in not added hashes.
            sc = new Scanner(Files.newInputStream(path), "US-ASCII");

            // Skipping header
            sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                processOsListLine(line);
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

    private void processOsListLine(String s) {
        // TODO remove duplicated code
        Matcher matcher = OS_PATTERN.matcher(s);
        try {
            matcher.matches();
            osList.put(matcher.group("osCode"), String.format("%s %s", matcher.group("osName"),matcher.group("osVersion")));
        } catch (IllegalStateException | UncheckedIOException e) {
            logger.warn("Could not parse line: {}", s);
        }
    }

    private void readProductList(Path path) throws IOException {
        productList = new HashMap<>();
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            // File is encoded in ASCII, but fileNames can be encoded different.
            // Using UTF-8 encoding may cause nextLine to break in this field, resulting in not added hashes.
            sc = new Scanner(Files.newInputStream(path), "US-ASCII");

            // Skipping header
            sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // TODO add to productList

                processProductListLine(line);
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

    private void processProductListLine(String s) {
        // TODO remove duplicated code
        Matcher matcher = PRODUCT_PATTERN.matcher(s);
        try {
            matcher.matches();
            productList.put(matcher.group("productCode"), String.format("%s %s", matcher.group("productName"),matcher.group("productVersion")));
        } catch (IllegalStateException | UncheckedIOException e) {
            logger.warn("Could not parse line: {}", s);
        }
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
        if(config == null) return;
        zipFile = Paths.get(config);
    }
}
