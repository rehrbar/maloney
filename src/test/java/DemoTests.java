import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by r1ehrbar on 17.10.2016.
 */
public class DemoTests {

    @Test
    public void HelloWorldTest(){
        assertEquals("Hello World!", Demo.HelloWorld());
    }
}
