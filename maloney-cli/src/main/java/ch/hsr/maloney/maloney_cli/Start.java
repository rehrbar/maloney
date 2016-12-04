package ch.hsr.maloney.maloney_cli;

import ch.hsr.maloney.core.FrameworkConfiguration;
import ch.hsr.maloney.core.FrameworkController;

import java.io.IOException;

/**
 * Entry point for the command line interface of Maloney.
 */
public class Start {
    public static void main(String[] args) {
        // Required override due to building problems of TSK on Windows 7 with .NET Framework >= 4.5 or newer Windows versions.
        // All libraries need to be inside java.library.path
        // Example: -Djava.library.path="C:\libs"
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.loadLibrary("zlib");
            System.loadLibrary("libewf");
            System.loadLibrary("libvmdk");
            System.loadLibrary("libvhdi");
            System.loadLibrary("libtsk_jni");
        }
        // TODO convert args to hash map to make them easier available
        // TODO allow overrides of FrameworkConfiguration
        // TODO pass working directory
        // TODO pass job configurations
        // TODO pass jobs to execute
        // TODO add generator for example configuration
        try {
            FrameworkConfiguration config = FrameworkConfiguration.loadFromFile(args[0]);
            FrameworkController.run(config);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO print example usage
        }
    }
}
