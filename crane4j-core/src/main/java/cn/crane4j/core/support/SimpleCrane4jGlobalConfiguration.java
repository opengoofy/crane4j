package cn.crane4j.core.support;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport;
import cn.crane4j.core.support.aop.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.support.aop.MethodResultAutoOperateSupport;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.callback.DefaultCacheableContainerProcessor;
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
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
     * Create a {@link SimpleCrane4jGlobalConfiguration} using the default configuration.
     *
     * @return configuration
     */
    public static SimpleCrane4jGlobalConfiguration create(@Nullable Map<String, String> cacheConfig) {
        SimpleCrane4jGlobalConfiguration configuration = new SimpleCrane4jGlobalConfiguration();
        // basic components
        PropertyOperator operator = new ReflectPropertyOperator();
        operator = new MapAccessiblePropertyOperator(operator);
        operator = new ChainAccessiblePropertyOperator(operator);
        configuration.setPropertyOperator(operator);
        configuration.setTypeResolver(new SimpleTypeResolver());
        CacheManager cacheManager = new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);

        // container register aware
        configuration.addContainerRegisterAware(new ContainerRegisteredLogger());
        if (MapUtil.isNotEmpty(cacheConfig)) {
            configuration.addContainerRegisterAware(new DefaultCacheableContainerProcessor(cacheManager, cacheConfig));
        }

        // operation parser
        AnnotationFinder finder = new SimpleAnnotationFinder();
        BeanOperationParser parser = new AnnotationAwareBeanOperationParser(finder, configuration);
        configuration.getBeanOperationParserMap().put(parser.getClass().getName(), parser);

        // operation executor
        DisorderedBeanOperationExecutor disorderedBeanOperationExecutor = new DisorderedBeanOperationExecutor();
        configuration.getBeanOperationExecutorMap().put(disorderedBeanOperationExecutor.getClass().getName(), disorderedBeanOperationExecutor);
        OrderedBeanOperationExecutor orderedBeanOperationExecutor = new OrderedBeanOperationExecutor(Sorted.comparator());
        configuration.getBeanOperationExecutorMap().put(orderedBeanOperationExecutor.getClass().getName(), orderedBeanOperationExecutor);

        // operation handler
        OneToOneReflexAssembleOperationHandler oneToOneReflexAssembleOperationHandler = new OneToOneReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(oneToOneReflexAssembleOperationHandler.getClass().getName(), oneToOneReflexAssembleOperationHandler);
        OneToManyReflexAssembleOperationHandler oneToManyReflexAssembleOperationHandler = new OneToManyReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(oneToManyReflexAssembleOperationHandler.getClass().getName(), oneToManyReflexAssembleOperationHandler);
        ManyToManyReflexAssembleOperationHandler manyToManyReflexAssembleOperationHandler = new ManyToManyReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(manyToManyReflexAssembleOperationHandler.getClass().getName(), manyToManyReflexAssembleOperationHandler);
        ReflectDisassembleOperationHandler reflectDisassembleOperationHandler = new ReflectDisassembleOperationHandler(operator);
        configuration.getDisassembleOperationHandlerMap().put(reflectDisassembleOperationHandler.getClass().getName(), reflectDisassembleOperationHandler);

        // expression
        ExpressionEvaluator evaluator = new OgnlExpressionEvaluator();

        // auto operate support
        ParameterNameFinder parameterNameFinder = new SimpleParameterNameFinder();
        MethodBaseExpressionExecuteDelegate expressionExecuteDelegate = new MethodBaseExpressionExecuteDelegate(
            parameterNameFinder, evaluator, method -> new OgnlExpressionContext()
        );
        MethodResultAutoOperateSupport methodResultAutoOperateSupport = new MethodResultAutoOperateSupport(configuration, expressionExecuteDelegate);
        MethodArgumentAutoOperateSupport methodArgumentAutoOperateSupport = new MethodArgumentAutoOperateSupport(
            configuration, expressionExecuteDelegate, parameterNameFinder, finder
        );

        // method container factory
        DefaultMethodContainerFactory defaultMethodContainerFactory = new DefaultMethodContainerFactory(operator, finder);
        CacheableMethodContainerFactory cacheableMethodContainerFactory = new CacheableMethodContainerFactory(operator, finder, cacheManager);

        // operate template
        OperateTemplate operateTemplate = new OperateTemplate(parser, disorderedBeanOperationExecutor, configuration.getTypeResolver());
        
        return configuration;
    }

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
