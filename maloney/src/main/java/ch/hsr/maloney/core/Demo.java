package ch.hsr.maloney.core;

import org.sleuthkit.datamodel.BlackboardArtifact;

/**
 * Created by r1ehrbar on 17.10.2016.
 */
public class Demo {
    // TODO remove this superlfuent class.
    public static String helloWorld() {
        return "Hello World!";
    }

    public static String testTsk() {
        BlackboardArtifact.Type artifact = new BlackboardArtifact.Type(1, "Name.Something", "This is something.");
        return artifact.getTypeName();
    }
}
