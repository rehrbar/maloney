package ch.hsr.maloney.core;

import ch.hsr.maloney.processing.TSKReadImageJob;

/**
 * Created by olive_000 on 08.11.2016.
 */
public class FrameworkController {
    public static void run(String imagePath) {
        Framework framework = new Framework();
        framework.register(new TSKReadImageJob());

        framework.startWithDisk(imagePath);
    }
}