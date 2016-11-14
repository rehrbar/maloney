package ch.hsr.maloney.maloney_cli;

import ch.hsr.maloney.core.FrameworkController;

/**
 * Created by r1ehrbar on 17.10.2016.
 */
public class Start {
    public static void main(String[] args) {
        System.loadLibrary("zlib");
        System.loadLibrary("libewf");
        System.loadLibrary("libvmdk");
        System.loadLibrary("libvhdi");
        System.loadLibrary("libtsk_jni");

        final String IMAGE_PATH = args[0];
        System.out.println("Starting framework with image in: " + IMAGE_PATH);
        FrameworkController.run(IMAGE_PATH);
        System.out.println("...FINISHED");
    }
}
