package ch.hsr.maloney.processing;

import ch.hsr.maloney.util.Context;
import ch.hsr.maloney.util.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by olive_000 on 15.11.2016.
 */
public class CalculateHashesJob implements Job {
    private final List<String> requiredEvents = new LinkedList<>();
    private final List<String> producedEvents = new LinkedList<>();
    private final Logger logger;

    private final String MD5HashCalculatedEventName = "MD5HashCalculated";
    private final String SHA1HashCalculatedEventName = "SHA1HashCalculated";

    public CalculateHashesJob() {
        logger = LogManager.getLogger("CalculateHashesJob");
        //TODO check Event names, maybe track globally?
        requiredEvents.add("newFile");
        producedEvents.add(MD5HashCalculatedEventName);
        producedEvents.add(SHA1HashCalculatedEventName);
    }

    @Override
    public boolean canRun(Context ctx, Event evt) {
        for(String jobName:requiredEvents){
            if(jobName.equals(evt.getName())){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Event> run(Context ctx, Event evt) {
        MessageDigest md5;
        MessageDigest sha1;

        byte[] md5digest;
        byte[] sha1digest;

        List<Event> events = new LinkedList<>();

        try {
            md5 = MessageDigest.getInstance("MD5");
            sha1 = MessageDigest.getInstance("SHA-1");

            //TODO get actual file
            InputStream is = Files.newInputStream(Paths.get("file.txt"));

            DigestInputStream md5dis = new DigestInputStream(is, md5);
            DigestInputStream sha1dis = new DigestInputStream(md5dis, sha1);

            while(sha1dis.read() != -1){
                /* Read decorated stream to EOF as normal... */
            }

            md5digest = md5.digest();
            sha1digest = sha1.digest();

            is.close();
        } catch (IOException e) {
            logger.error("Could not read file with UUID: " + evt.getFileUuid().toString(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not find specified Algorithm to calculate Hash", e);
        }
        //TODO add Hashes as artifact to MetaDataStore
        

        events.add(new Event(MD5HashCalculatedEventName, this.getJobName(), evt.getFileUuid()));
        events.add(new Event(SHA1HashCalculatedEventName, this.getJobName(), evt.getFileUuid()));

        return events;
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
        return "CalculateHashesJob";
    }

    @Override
    public String getJobConfig() {
        return null;
        //TODO not necessary as of yet
    }
}
