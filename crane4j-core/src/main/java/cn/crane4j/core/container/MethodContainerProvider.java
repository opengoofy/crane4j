package cn.crane4j.core.container;

import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author huangchengxing
 * @see ContainerMethodAnnotationProcessor
 */
@RequiredArgsConstructor
public class MethodContainerProvider implements ContainerProvider {

    private final ContainerMethodAnnotationProcessor processor;

    /**
     * Get container instance by given namespace
     *
     * @param namespace namespace of container
     * @return container instance
     */
    @Nullable
    @Override
    public <K> Container<K> getContainer(String namespace) {

        return null;
    }
}
