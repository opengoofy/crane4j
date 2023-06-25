package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link DefaultContainerManager}
 *
 * @author huangchengxing
 */
public class DefaultContainerManagerTest {

    private ContainerManager containerManager;

    @Before
    public void init() {
        containerManager = new DefaultContainerManager();
    }

    @Test
    public void containerLifecycleProcessorTest() {
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() {};
        containerManager.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(1, containerManager.getContainerLifecycleProcessors().size());
    }

    @Test
    public void containerProviderTest() {
        containerManager.registerContainerLifecycleProcessor(new ContainerLifecycleProcessor() {});
        Container<Object> container = Container.empty();
        ContainerProvider provider = new ContainerProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public @Nullable <K> Container<K> getContainer(String namespace) {
                return (Container<K>) container;
            }
        };

        // register provider
        containerManager.registerContainerProvider("test", provider);
        Assert.assertSame(provider, containerManager.getContainerProvider("test"));

        // get container by provider
        Assert.assertSame(container, containerManager.getContainer("test", container.getNamespace()));
        String canonicalNamespace = ContainerManager.canonicalNamespace(container.getNamespace(), "test");
        Assert.assertTrue(containerManager.containsContainer(canonicalNamespace));
        Assert.assertSame(container, containerManager.getContainer(canonicalNamespace));

        // the provider is not registered, we get null
        Assert.assertNull(containerManager.getContainer("test2", container.getNamespace()));
    }

    @Test
    public void containerTest() {
        Container<Object> container = LambdaContainer.forLambda("test", ids -> Collections.emptyMap());
        ContainerDefinition definition = ContainerDefinition.create("test", "test", () -> container);

        // register container by definition
        Object old = containerManager.registerContainer(definition);
        Assert.assertNull(old);
        Assert.assertTrue(containerManager.containsContainer(definition.getNamespace()));
        Assert.assertSame(container, containerManager.getContainer(definition.getNamespace()));

        // register container by factory method
        old = containerManager.registerContainer("test", () -> container);
        Assert.assertSame(old, container);
        Assert.assertTrue(containerManager.containsContainer(definition.getNamespace()));
        Assert.assertSame(container, containerManager.getContainer(definition.getNamespace()));

        // register container by instance
        old = containerManager.registerContainer(container);
        Assert.assertSame(old, container);
        Assert.assertTrue(containerManager.containsContainer(definition.getNamespace()));
        Assert.assertSame(container, containerManager.getContainer(definition.getNamespace()));
    }

    @Test
    public void clear() {
        // register some container、provider、processor
        Container<Object> container = Container.empty();
        ContainerProvider provider = new ContainerProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public @Nullable <K> Container<K> getContainer(String namespace) {
                return (Container<K>) container;
            }
        };
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() {};
        containerManager.registerContainerLifecycleProcessor(processor);
        containerManager.registerContainerProvider("test", provider);
        containerManager.registerContainer(container);

        // clear all
        containerManager.clear();
        Assert.assertTrue(containerManager.getContainerLifecycleProcessors().isEmpty());
        Assert.assertNull(containerManager.getContainerProvider("test"));
        Assert.assertFalse(containerManager.containsContainer(container.getNamespace()));
    }
}
