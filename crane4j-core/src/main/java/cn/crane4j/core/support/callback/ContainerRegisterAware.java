package cn.crane4j.core.support.callback;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The callback function before the {@link Container} is registered to {@link ContainerProvider}.
 *
 * @author huangchengxing
 */
public interface ContainerRegisterAware {

    /**
     * Called before {@link Container} is registered to {@link ContainerProvider}.<br />
     * If the return value is {@code null}, the registration of the container will be abandoned
     *
     * @param operator caller of the current method
     * @param container container
     * @return {@link Container} who really wants to be registered to {@link ContainerProvider}
     */
    @Nullable
    default Container<?> beforeContainerRegister(Object operator, @Nonnull Container<?> container) {
        return container;
    }

    /**
     * Called after {@link Container} is registered to {@link ContainerProvider}
     *
     * @param operator caller of the current method
     * @param container container
     */
    default void afterContainerRegister(Object operator, @Nonnull Container<?> container) {
        // do nothing
    }
}
