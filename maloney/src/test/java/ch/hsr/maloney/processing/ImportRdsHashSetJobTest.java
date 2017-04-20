package ch.hsr.maloney.processing;

import ch.hsr.maloney.storage.hash.HashAlgorithm;
import ch.hsr.maloney.storage.hash.HashRecord;
import ch.hsr.maloney.storage.hash.HashType;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.hsr.maloney.processing.ImportRdsHashSetJob.FILE_PATTERN;

/**
 * Created by r1ehrbar on 28.11.2016.
 */
public class ImportRdsHashSetJobTest {
    @Test
    public void runTest() throws JobCancelledException {
        Job job = new ImportRdsHashSetJob();
        job.setJobConfig("D:\\hash_lists\\rds_254u_100k.zip");
        Assert.assertTrue(job.canRun(null, null));
        job.run(null, null);
    }

    @Test
    public void matcherTest(){
        String s = "\"0000000F8527DCCAB6642252BBCFA1B8072D33EE\",\"68CE322D8A896B6E4E7E3F18339EC85C\",\"E39149E4\",\"Blended_Coolers_Vanilla_NL.png\",30439,28948,\"358\",\"\"";
        Matcher matcher = FILE_PATTERN.matcher(s);
        matcher.matches();
        Assert.assertEquals(8, matcher.groupCount());
        Assert.assertEquals("0000000F8527DCCAB6642252BBCFA1B8072D33EE", matcher.group("sha1"));
        Assert.assertEquals("68CE322D8A896B6E4E7E3F18339EC85C", matcher.group("md5"));
        Assert.assertEquals("E39149E4", matcher.group("crc32"));
    }
}
