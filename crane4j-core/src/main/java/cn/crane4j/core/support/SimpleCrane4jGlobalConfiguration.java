package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Basic implementation of {@link Crane4jGlobalConfiguration}.
 *
 * @author huangchengxing
 */
@Getter
public class SimpleCrane4jGlobalConfiguration implements Crane4jGlobalConfiguration {

    @Setter
    private TypeResolver typeResolver;
    @Setter
    private PropertyOperator propertyOperator;
    @Getter
    private final List<ContainerRegisterAware> containerRegisterAwareList = new ArrayList<>(4);
    private final Map<String, Container<?>> containerMap = new HashMap<>(16);
    private final Map<String, BeanOperationParser> beanOperationParserMap = new HashMap<>(16);
    private final Map<String, AssembleOperationHandler> assembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, DisassembleOperationHandler> disassembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, BeanOperationExecutor> beanOperationExecutorMap = new HashMap<>(4);
    private final Map<String, ContainerProvider> containerProviderMap = new HashMap<>(4);

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    @Override
    public Container<?> getContainer(String namespace) {
        return Assert.notNull(
            containerMap.get(namespace),
            () -> new Crane4jException("cannot find container [{}]", namespace)
        );
    }

    /**
     * Add a {@link ContainerRegisterAware} callback.
     *
     * @param containerRegisterAware callback
     */
    @Override
    public void addContainerRegisterAware(ContainerRegisterAware containerRegisterAware) {
        containerRegisterAwareList.remove(containerRegisterAware);
        containerRegisterAwareList.add(containerRegisterAware);
    }

    /**
     * Whether the container has been registered.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        return containerMap.containsKey(namespace);
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
     */
    @Nullable
    @Override
    public Container<?> replaceContainer(String namespace, UnaryOperator<Container<?>> replacer) {
        Container<?> prev = containerMap.remove(namespace);
        Container<?> next = replacer.apply(prev);
        if (Objects.nonNull(next)) {
            containerMap.put(namespace, next);
        }
        return prev;
    }

    /**
     * Register container.
     *
     * @param container container
     * @throws Crane4jException thrown when the namespace of the container has been registered
     */
    @Override
    public void registerContainer(Container<?> container) {
        ConfigurationUtil.registerContainer(
            this, containerMap::get, c -> containerMap.put(c.getNamespace(), c),
            container, getContainerRegisterAwareList()
        );
    }

    /**
     * Get container provider.
     *
     * @param providerType provider type
     * @return provider
     */
    @Override
    public ContainerProvider getContainerProvider(Class<? extends ContainerProvider> providerType) {
        return getContainerProvider(providerType.getName());
    }

    /**
     * Get container provider.
     *
     * @param providerName provider name
     * @return provider
     */
    @Override
    public ContainerProvider getContainerProvider(String providerName) {
        return containerProviderMap.get(providerName);
    }

    /**
     * Get bean operation executor.
     *
     * @param executorType executor type
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType) {
        return getBeanOperationExecutor(executorType.getName());
    }

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        BeanOperationExecutor executor = beanOperationExecutorMap.get(executorName);
        Assert.notNull(executor, () -> new Crane4jException("cannot find executor [{}]", executorName));
        return executor;
    }

    /**
     * Get bean operation parser.
     *
     * @param parserType parser type
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType) {
        return getBeanOperationsParser(parserType.getName());
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        BeanOperationParser parser = beanOperationParserMap.get(parserName);
        Assert.notNull(parser, () -> new Crane4jException("cannot find parser [{}]", parserName));
        return parser;
    }

    /**
     * Get assemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType) {
        return getAssembleOperationHandler(handlerType.getName());
    }

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        AssembleOperationHandler handler = assembleOperationHandlerMap.get(handlerName);
        Assert.notNull(handler, () -> new Crane4jException("cannot find handler [{}]", handlerName));
        return handler;
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType) {
        return getDisassembleOperationHandler(handlerType.getName());
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        DisassembleOperationHandler handler = disassembleOperationHandlerMap.get(handlerName);
        Assert.notNull(handler, () -> new Crane4jException("cannot find handler [{}]", handlerName));
        return handler;
    }
}
