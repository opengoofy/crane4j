package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.EmptyContainer;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * <p>This class serves as the top-level template class
 * and defines the key steps required by most {@link AssembleOperationHandler}:<br />
 * Object processing phase:
 * <ul>
 *     <li>
 *         {@link #collectToEntities}: Expands the target objects to be operated on from the {@link AssembleExecution}
 *         and wraps them as {@link Target} objects, which will be used for subsequent processing;
 *     </li>
 *     <li>
 *         {@link #introspectForEntities}: If the {@link Container} for the current operation is not specified,
 *         directly use the target objects as the data source for field mapping;
 *     </li>
 * </ul>
 * If the {@link Container} for the current operation is specified, it enters the data source preparation phase:
 * <ul>
 *     <li>
 *         {@link #getSourcesFromContainer}: Obtains the required data sources based on the objects to be processed;
 *     </li>
 *     <li>
 *         {@link #getTheAssociatedSource}: Retrieves the associated data source object
 *         corresponding to the key value of the object to be processed from the data sources;
 *     </li>
 * </ul>
 * Finally, if the object has an associated data source object,
 * the {@link #completeMapping} method is called to perform property mapping between them.
 *
 * <p>The implementation logic of this template class is based on
 * the encapsulation of {@link Target}, which may introduce unnecessary performance overhead.
 *
 * @author huangchengxing
 * @param <T> target type
 */
public abstract class ReflectiveAssembleOperationHandler<T extends ReflectiveAssembleOperationHandler.Target> implements AssembleOperationHandler {

    /**
     * Perform assembly operation.
     *
     * @param container  container
     * @param executions operations to be performed
     */
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        Collection<T> targets = collectToEntities(executions);
        if (container instanceof EmptyContainer || Objects.isNull(container)) {
            introspectForEntities(targets);
            return;
        }
        Map<Object, Object> sources = getSourcesFromContainer(container, targets);
        if (CollectionUtils.isEmpty(sources)) {
            return;
        }
        for (T target : targets) {
            Object source = getTheAssociatedSource(target, sources);
            if (ObjectUtils.isNotEmpty(source)) {
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

        /**
         * value of key property
         */
        private final Object key;
    }
}
