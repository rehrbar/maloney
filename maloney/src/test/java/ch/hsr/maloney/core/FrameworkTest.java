package ch.hsr.maloney.core;

import ch.hsr.maloney.util.FakeJobFactory;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by olive_000 on 22.11.2016.
 */
public class FrameworkTest {

    @Test
    public void simpleDependencyTest(){
        Framework framework = new Framework(null);
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        framework.register(fakeJobFactory.getAJob());
        framework.register(fakeJobFactory.getAtoBJob());

        try {
            framework.checkDependencies();
        } catch (Framework.UnrunnableJobException e) {
            fail();
        }
    }

    @Test(expected = Framework.UnrunnableJobException.class)
    public void advancedDependencyTest() throws Framework.UnrunnableJobException{
        Framework framework = new Framework(null);
        FakeJobFactory fakeJobFactory = new FakeJobFactory();
        framework.register(fakeJobFactory.getAJob());
        framework.register(fakeJobFactory.getAtoBJob());

        framework.checkDependencies();
        fail();
    }
}
