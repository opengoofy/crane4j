package cn.crane4j.core.support;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.DynamicSourceContainerProvider;
import cn.crane4j.core.container.SharedContextContainerProvider;
import cn.crane4j.core.container.SimpleConfigurableContainerProvider;
import cn.crane4j.core.container.ThreadContextContainerProvider;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AssembleAnnotationResolver;
import cn.crane4j.core.parser.AssembleEnumAnnotationResolver;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.DisassembleAnnotationResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.callback.ContainerRegisteredLogger;
import cn.crane4j.core.support.callback.DefaultCacheableContainerProcessor;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.map.MapUtil;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of {@link Crane4jGlobalConfiguration}.
 *
 * @author huangchengxing
 */
@Getter
public class SimpleCrane4jGlobalConfiguration
    extends SimpleConfigurableContainerProvider implements Crane4jGlobalConfiguration {

    @Setter
    private TypeResolver typeResolver;
    @Setter
    private PropertyOperator propertyOperator;
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
    public static SimpleCrane4jGlobalConfiguration create() {
        return create(Collections.emptyMap());
    }

    /**
     * Create a {@link SimpleCrane4jGlobalConfiguration} using the default configuration.
     *
     * @param cacheConfig cacheConfig
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
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        BeanOperationParser beanOperationParser = new TypeHierarchyBeanOperationParser(Arrays.asList(
            new AssembleAnnotationResolver(annotationFinder, configuration),
            new DisassembleAnnotationResolver(annotationFinder, configuration),
            new AssembleEnumAnnotationResolver(annotationFinder, configuration, operator, configuration)
        ));
        configuration.getBeanOperationParserMap().put(BeanOperationParser.class.getName(), beanOperationParser);
        configuration.getBeanOperationParserMap().put(beanOperationParser.getClass().getName(), beanOperationParser);

        // operation executor
        DisorderedBeanOperationExecutor disorderedBeanOperationExecutor = new DisorderedBeanOperationExecutor();
        configuration.getBeanOperationExecutorMap().put(BeanOperationExecutor.class.getName(), disorderedBeanOperationExecutor);
        configuration.getBeanOperationExecutorMap().put(disorderedBeanOperationExecutor.getClass().getName(), disorderedBeanOperationExecutor);
        OrderedBeanOperationExecutor orderedBeanOperationExecutor = new OrderedBeanOperationExecutor(Sorted.comparator());
        configuration.getBeanOperationExecutorMap().put(orderedBeanOperationExecutor.getClass().getName(), orderedBeanOperationExecutor);

        // operation handler
        OneToOneReflexAssembleOperationHandler oneToOneReflexAssembleOperationHandler = new OneToOneReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(AssembleOperationHandler.class.getName(), oneToOneReflexAssembleOperationHandler);
        configuration.getAssembleOperationHandlerMap().put(oneToOneReflexAssembleOperationHandler.getClass().getName(), oneToOneReflexAssembleOperationHandler);
        OneToManyReflexAssembleOperationHandler oneToManyReflexAssembleOperationHandler = new OneToManyReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(oneToManyReflexAssembleOperationHandler.getClass().getName(), oneToManyReflexAssembleOperationHandler);
        ManyToManyReflexAssembleOperationHandler manyToManyReflexAssembleOperationHandler = new ManyToManyReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(manyToManyReflexAssembleOperationHandler.getClass().getName(), manyToManyReflexAssembleOperationHandler);
        ReflectDisassembleOperationHandler reflectDisassembleOperationHandler = new ReflectDisassembleOperationHandler(operator);
        configuration.getDisassembleOperationHandlerMap().put(DisassembleOperationHandler.class.getName(), reflectDisassembleOperationHandler);
        configuration.getDisassembleOperationHandlerMap().put(reflectDisassembleOperationHandler.getClass().getName(), reflectDisassembleOperationHandler);

        // container provider
        configuration.getContainerProviderMap().put(configuration.getClass().getName(), configuration);
        configuration.getContainerProviderMap().put(ContainerProvider.class.getName(), configuration);
        ThreadContextContainerProvider threadContextContainerProvider = new ThreadContextContainerProvider();
        configuration.getContainerProviderMap().put(DynamicSourceContainerProvider.class.getName(), threadContextContainerProvider);
        configuration.getContainerProviderMap().put(threadContextContainerProvider.getClass().getName(), threadContextContainerProvider);
        SharedContextContainerProvider sharedContextContainerProvider = new SharedContextContainerProvider();
        configuration.getContainerProviderMap().put(sharedContextContainerProvider.getClass().getName(), sharedContextContainerProvider);

        return configuration;
    }

    /**
     * Get container provider.
     *
     * @param providerType provider type
     * @return provider
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ContainerProvider> T getContainerProvider(Class<T> providerType) {
        return (T)getContainerProvider(providerType.getName());
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
        Asserts.isNotNull(executor, "cannot find executor [{}]", executorName);
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
        Asserts.isNotNull(parser, "cannot find parser [{}]", parserName);
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
        Asserts.isNotNull(handler, "cannot find handler [{}]", handlerName);
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
        Asserts.isNotNull(handler, "cannot find handler [{}]", handlerName);
        return handler;
    }
}
