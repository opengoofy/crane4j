package cn.crane4j.core.container;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link EmptyContainer}
 *
 * @author huangchengxing
 */
public class EmptyContainerTest {

    @Test
    public void get() {
        Container<Object> container = Container.empty();
        Assert.assertSame(container, Container.empty());
        Assert.assertEquals(EmptyContainer.NAMESPACE, container.getNamespace());
        Assert.assertTrue(container.get(null).isEmpty());
    }

}
