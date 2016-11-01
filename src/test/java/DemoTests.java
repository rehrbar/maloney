import ch.hsr.maloney.core.Demo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by r1ehrbar on 17.10.2016.
 */
public class DemoTests {

    @Test
    public void helloWorldTest() {
        assertEquals("Hello World!", Demo.helloWorld());
    }

    @Test
    public void tskTest() {
        assertEquals("Name.Something", Demo.testTsk());
    }
}
