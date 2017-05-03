package ch.hsr.maloney.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FrameworkControllerTest {
    private FrameworkController controller;
    private Path workingDirectory;

    @Before
    public void setup() throws IOException {
        workingDirectory = Files.createTempDirectory("maloney-test");
        FrameworkConfiguration config = new FrameworkConfiguration();
        config.setWorkingDirectory(workingDirectory.toString());
        controller = new FrameworkController(config);
    }

    @After
    public void teardown() throws IOException{
        Files.deleteIfExists(workingDirectory);
    }

    @Test
    public void GetInitialCaseIdentifier(){
        String identifier = controller.getCaseIdentifier();
        Assert.assertNotNull(identifier);
        Assert.assertTrue(identifier.length() > 0);
    }

    @Test
    public void GetInitialCaseDirectory(){
        Path workingDirectory = controller.getCaseDirectory();
        Assert.assertNotNull(workingDirectory);
    }

    @Test
    public void SetCaseIdentifier(){
        String newIdentifier = "identifier-1";
        controller.setCaseIdentifier(newIdentifier);
        String identifier = controller.getCaseIdentifier();
        Assert.assertEquals(newIdentifier, identifier);
    }

    @Test(expected = IllegalArgumentException.class)
    public void SetInvalidIdentifier(){
        String newIdentifier = "identi_fier1";
        controller.setCaseIdentifier(newIdentifier);
    }

    @Test
    public void GeneratedIdentifierExistingDirectory() throws IOException {
        Files.createDirectories(workingDirectory.resolve("maloney1"));
        Files.createDirectories(workingDirectory.resolve("maloney2"));
        controller.setWorkingDirectory(workingDirectory.toString());
        String identifier = controller.getCaseIdentifier();
        Assert.assertEquals("maloney3", identifier);
    }
}
