package cn.crane4j.core.support.callback;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

/**
 * Output log information after the {@link Container} is registered successfully.
 *
 * @author huangchengxing
 */
@Slf4j
public class ContainerRegisteredLogger implements ContainerRegisterAware {

    /**
     * Called after {@link Container} is registered to {@link ContainerProvider}
     *
     * @param operator  caller of the current method
     * @param container container
     */
    @Override
    public void afterContainerRegister(Object operator, @Nonnull Container<?> container) {
        log.info("container [{}] is registered by [{}]", container.getNamespace(), operator);
    }
}
