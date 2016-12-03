package ch.hsr.maloney.maloney_cli;

import ch.hsr.maloney.core.FrameworkController;

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

        final String IMAGE_PATH = args[0];
        System.out.println("Starting framework with image in: " + IMAGE_PATH);
        FrameworkController.run(IMAGE_PATH);
        System.out.println("...FINISHED");
    }
}
