package cn.crane4j.core.container;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReadWriteLockSupport;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * <p>Simple implementation of {@link ConfigurableContainerProvider},
 * providing thread-safe container registration, replacement, deletion, and other operations,
 * as well as support for ContainerRegisterAware callbacks.
 *
 * @author huangchengxing
 */
public class SimpleConfigurableContainerProvider implements ConfigurableContainerProvider {

    private final ReadWriteLockSupport lock = new ReadWriteLockSupport(new ReentrantReadWriteLock());
    private final List<ContainerRegisterAware> containerRegisterAwareList = new ArrayList<>();
    protected final Map<String, Container<?>> containerMap = new HashMap<>();

    /**
     * Add a {@link ContainerRegisterAware} callback.
     *
     * @param containerRegisterAware callback
     */
    @Override
    public void addContainerRegisterAware(ContainerRegisterAware containerRegisterAware) {
        lock.withWriteLock(() -> {
            containerRegisterAwareList.remove(containerRegisterAware);
            containerRegisterAwareList.add(containerRegisterAware);
        });
    }

    /**
     * Get container if exists, otherwise create a new container.
     *
     * @param namespace namespace
     * @param containerFactory container factory
     * @return container
     */
    @Nullable
    @Override
    public Container<?> getContainer(String namespace, Supplier<Container<?>> containerFactory) {
        Container<?> old = containerMap.get(namespace);
        if (Objects.nonNull(old)) {
            return old;
        }
        return lock.withWriteLock(() -> {
            Container<?> container = containerMap.get(namespace);
            if (Objects.nonNull(container)) {
                return container;
            }
            container = containerFactory.get();
            Asserts.isNotNull(container, "container factory returned null");
            String containerNamespace = container.getNamespace();
            Asserts.isEquals(
                containerNamespace, namespace,
                "The namespace of the current container [{}] is inconsistent with that of the old container [{}]",
                containerNamespace, namespace
            );
            ConfigurationUtil.invokeRegisterAware(
                this, container, getContainerRegisterAwareList(), c -> containerMap.put(namespace, c)
            );
            return container;
        });
    }

    /**
     * Get all registered {@link ContainerRegisterAware} callback.
     *
     * @return registered {@link ContainerRegisterAware}
     */
    public List<ContainerRegisterAware> getContainerRegisterAwareList() {
        return lock.withReadLock(() -> containerRegisterAwareList);
    }

    /**
     * Replace the registered container.
     * <ul>
     *     <li>if the container is not registered, it will be added;</li>
     *     <li>if {@code replacer} return {@code null}, the old container will be deleted;</li>
     * </ul>
     *
     * @param namespace namespace
     * @param replacer  replacer
     * @return old container
     * @see Map#compute(Object, BiFunction)
     */
    @Nullable
    @Override
    public Container<?> compute(String namespace, UnaryOperator<Container<?>> replacer) {
        return lock.withWriteLock(() -> {
            Container<?> prev = containerMap.remove(namespace);
            Container<?> next = replacer.apply(prev);
            // just remove old only
            if (Objects.isNull(next)) {
                return prev;
            }
            // register new container
            if (!Objects.equals(namespace, next.getNamespace())) {
                throw new Crane4jException(
                    "The namespace of the current container [{}] is inconsistent with that of the old container [{}]",
                    next.getNamespace(), namespace
                );
            }
            ConfigurationUtil.invokeRegisterAware(
                this, next, getContainerRegisterAwareList(), c -> containerMap.put(namespace, c)
            );
            containerMap.put(namespace, next);
            return prev;
        });
    }

    /**
     * Register container.
     *
     * @param container container
     * @throws Crane4jException thrown when the namespace of the container has been registered
     * @see ContainerRegisterAware
     */
    @Override
    public void registerContainer(Container<?> container) {
        lock.withWriteLock(() -> {
            String namespace = container.getNamespace();
            Asserts.isFalse(containerMap.containsKey(namespace), "the container [{}] has been registered", namespace);
            ConfigurationUtil.invokeRegisterAware(
                this, container, containerRegisterAwareList, c -> containerMap.put(namespace, c)
            );
        });
    }

    /**
     * Whether the container has been registered.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        return lock.withReadLock(() -> containerMap.containsKey(namespace));
    }

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     * @throws Crane4jException thrown when the container is not registered
     */
    @Override
    public Container<?> getContainer(String namespace) {
        Container<?> container = lock.withReadLock(() -> containerMap.get(namespace));
        Asserts.isNotNull(container, "the container [{}] is not registered", namespace);
        return container;
    }
}
