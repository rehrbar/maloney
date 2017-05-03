package ch.hsr.maloney.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FrameworkControllerTest {
    private FrameworkController controller;

    @Before
    public void setup(){
        controller = new FrameworkController();
    }

    @Test
    public void GetInitialCaseIdentifier(){
        FrameworkController controller = new FrameworkController();
        String identifier = controller.getCaseIdentifier();
        Assert.assertNotNull(identifier);
        Assert.assertTrue(identifier.length() > 0);
    }

    @Test
    public void GetInitialCaseDirectory(){
        FrameworkController controller = new FrameworkController();
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
        Path tmpDir = Files.createTempDirectory("maloney-test");
        tmpDir.toFile().deleteOnExit();
        Files.createDirectories(tmpDir.resolve("maloney1"));
        Files.createDirectories(tmpDir.resolve("maloney2"));
        controller.setWorkingDirectory(tmpDir.toString());
        String identifier = controller.getCaseIdentifier();
        Assert.assertEquals("maloney3", identifier);
    }
}
