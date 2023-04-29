package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.EmptyContainer;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract template implementation of {@link AssembleOperationHandler}.
 *
 * @author huangchengxing
 * @param <T> target type
 */
public abstract class AbstractAssembleOperationHandler<T extends AbstractAssembleOperationHandler.Target> implements AssembleOperationHandler {

    /**
     * Perform assembly operation.
     *
     * @param container  container
     * @param executions operations to be performed
     */
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        Collection<T> targets = collectToEntities(executions);
        if (container instanceof EmptyContainer) {
            introspectForEntities(targets);
            return;
        }
        Map<Object, Object> sources = getSourcesFromContainer(container, targets);
        if (CollectionUtils.isEmpty(sources)) {
            return;
        }
        for (T target : targets) {
            Object source = getTheAssociatedSource(target, sources);
            if (ObjectUtil.isNotEmpty(source)) {
                completeMapping(source, target);
            }
        }
    }

    /**
     * Split the {@link AssembleExecution} into pending objects and wrap it as {@link Target}.
     *
     * @param executions executions
     * @return {@link Target}
     */
    protected abstract Collection<T> collectToEntities(Collection<AssembleExecution> executions);

    /**
     * When the container is {@link EmptyContainer}, introspect the object to be processed.
     *
     * @param targets targets
     */
    protected void introspectForEntities(Collection<T> targets) {
        for (T target : targets) {
            completeMapping(target.getOrigin(), target);
        }
    }

    /**
     * Obtain the corresponding data source object from the data source container based on the entity's key value.
     *
     * @param container container
     * @param targets targets
     * @return source objects
     */
    protected abstract Map<Object, Object> getSourcesFromContainer(Container<?> container, Collection<T> targets);

    /**
     * Get the data source object associated with the target object.
     *
     * @param target target
     * @param sources sources
     * @return data source object associated with the target object
     */
    protected abstract Object getTheAssociatedSource(T target, Map<Object, Object> sources);

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    protected abstract void completeMapping(Object source, T target);

    /**
     * Target object to be processed.
     */
    @Getter
    @RequiredArgsConstructor
    protected static class Target {

        /**
         * execution
         */
        private final AssembleExecution execution;

        /**
         * objects to be processed
         */
        protected final Object origin;
    }
}
