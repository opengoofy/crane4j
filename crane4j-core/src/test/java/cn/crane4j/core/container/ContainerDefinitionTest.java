package cn.crane4j.core.container;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * test for {@link ContainerDefinition}
 *
 * @author huangchengxing
 */
public class ContainerDefinitionTest {

    @Test
    public void test() {
        Supplier<Container<Object>> containerFactory = () -> null;
        ContainerDefinition containerDefinition = ContainerDefinition.create("test", "test", containerFactory);
        Assert.assertEquals("test", containerDefinition.getNamespace());
        Assert.assertEquals("test", containerDefinition.getProviderName());
        Assert.assertSame(containerFactory, containerDefinition.getContainerFactory());
        // reset container factory
        containerDefinition.setContainerFactory(() -> null);
        Assert.assertNotSame(containerFactory, containerDefinition.getContainerFactory());
    }
}
