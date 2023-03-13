package cn.crane4j.core.support.callback;

import cn.crane4j.core.container.Container;
import org.junit.Assert;
import org.junit.Test;

/**
 * test default method of {@link ContainerRegisterAware}
 *
 * @author huangchengxing
 */
public class ContainerRegisterAwareTest {

    @Test
    public void test() {
        ContainerRegisterAware aware = new ContainerRegisterAware() { };
        Container<?> container = Container.empty();
        aware.afterContainerRegister(this, container);
        Assert.assertSame(container, aware.beforeContainerRegister(this, container));
    }
}
