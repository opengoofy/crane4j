package cn.crane4j.core.container;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * test for {@link SimpleConfigurableContainerProvider}
 *
 * @author huangchengxing
 */
public class SimpleConfigurableContainerProviderTest {

    private SimpleConfigurableContainerProvider containerProvider;

    @Before
    public void init() {
        containerProvider = new SimpleConfigurableContainerProvider();
        Container<?> container = LambdaContainer.forLambda("test", ids -> Collections.emptyMap());
        containerProvider.registerContainer(container);
    }

    public void getContainer() {
        Container<?> container = containerProvider.getContainer("test");
        Assert.assertNotNull(container);
        Assert.assertEquals("test", container.getNamespace());
        Assert.assertNull(containerProvider.getContainer("no registered"));

        container = containerProvider.getContainer("create if null", () -> LambdaContainer.forLambda("create if null", ids -> Collections.emptyMap()));
        Assert.assertNotNull(container);
    }

    @Test
    public void addContainerRegisterAware() {
        Collection<ContainerRegisterAware> awareList = containerProvider.getContainerRegisterAwareList();
        int size = awareList.size();
        ContainerRegisterAware aware = new ContainerRegisterAware() { };
        containerProvider.addContainerRegisterAware(aware);
        Assert.assertEquals(size + 1, awareList.size());
        containerProvider.addContainerRegisterAware(aware);
        Assert.assertEquals(size + 1, awareList.size());
    }

    @Test
    public void containsContainer() {
        Assert.assertTrue(containerProvider.containsContainer("test"));
        Assert.assertFalse(containerProvider.containsContainer("no registered"));
    }

    @Test
    public void registerContainer() {
        Assert.assertFalse(containerProvider.containsContainer("test register"));
        containerProvider.registerContainer(LambdaContainer.forLambda("test register", ids -> Collections.emptyMap()));
        Assert.assertTrue(containerProvider.containsContainer("test register"));
    }

    @Test
    public void replaceContainer() {
        Container<?> container1 = LambdaContainer.forLambda("container", ids -> Collections.emptyMap());

        // container first add
        Container<?> old = containerProvider.compute("container", container -> {
            Assert.assertNull(container);
            return null;
        });
        Assert.assertNull(old);

        // container first add
        old = containerProvider.compute("container", container -> {
            Assert.assertNull(container);
            return container1;
        });
        Assert.assertNull(old);

        // container replace
        Container<?> container2 = LambdaContainer.forLambda("container", ids -> Collections.emptyMap());
        old = containerProvider.compute("container", container -> {
            Assert.assertSame(container1, container);
            return container2;
        });
        Assert.assertSame(container1, old);

        Assert.assertSame(container2, containerProvider.getContainer("container"));
        Assert.assertFalse(containerProvider.containsContainer("no registered"));

        Assert.assertThrows(Crane4jException.class, () -> {
            containerProvider.compute("container", container ->
                LambdaContainer.forLambda("otherName", ids -> Collections.emptyMap())
            );
        });
    }
}
