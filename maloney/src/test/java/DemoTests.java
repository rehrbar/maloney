import ch.hsr.maloney.core.Demo;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Test
    public void HashMapTest(){
        UUID key1 = UUID.fromString("f99f4262-7b84-440a-b650-ccdd30940511");
        UUID key2 = UUID.fromString("f99f4262-7b84-440a-b650-ccdd30940511");
        // Key needs to implement hashCode() and equals() for this to work
        // UUID provides both methods correctly.
        Map<UUID, String> map = new HashMap<>();
        map.put(key1, "Key1");
        map.put(key2, "Key2"); // Key2 replaces Key1
        assertEquals("Key2", map.get(key1));
        assertEquals("Key2", map.get(key2));
    }
}
