package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ContainerRegisterLogger}
 *
 * @author huangchengxing
 */
@Slf4j
public class ContainerRegisterLoggerTest {

    @Test
    public void test() {
        ContainerRegisterLogger containerRegisterLogger = new ContainerRegisterLogger(log::info);
        Container<Object> container = Container.empty();
        ContainerDefinition containerDefinition = ContainerDefinition.create("test", "test", () -> container);

        // when registered
        ContainerDefinition definition = containerRegisterLogger.whenRegistered("test", containerDefinition);
        Assert.assertSame(containerDefinition, definition);
        definition = containerRegisterLogger.whenRegistered(null, containerDefinition);
        Assert.assertSame(containerDefinition, definition);

        // when created
        Container<Object> container2 = containerRegisterLogger.whenCreated(containerDefinition, container);
        Assert.assertSame(container, container2);

        // when destroyed
        containerRegisterLogger.whenDestroyed(containerDefinition);
        containerRegisterLogger.whenDestroyed(container);
    }
}
