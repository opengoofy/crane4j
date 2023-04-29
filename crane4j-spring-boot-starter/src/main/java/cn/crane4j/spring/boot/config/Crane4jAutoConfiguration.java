package cn.crane4j.spring.boot.config;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.ConfigurableContainerProvider;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.SharedContextContainerProvider;
import cn.crane4j.core.container.ThreadContextContainerProvider;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AssembleAnnotationResolver;
import cn.crane4j.core.parser.AssembleEnumAnnotationResolver;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.DisassembleAnnotationResolver;
import cn.crane4j.core.parser.OperationAnnotationResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.callback.ContainerRegisteredLogger;
import cn.crane4j.core.support.callback.DefaultCacheableContainerProcessor;
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.support.operator.DefaultProxyMethodFactory;
import cn.crane4j.core.support.operator.DynamicSourceProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.mybatis.plus.AssembleMpAnnotationResolver;
import cn.crane4j.extension.spring.BeanMethodContainerRegistrar;
import cn.crane4j.extension.spring.Crane4jApplicationContext;
import cn.crane4j.extension.spring.MergedAnnotationFinder;
import cn.crane4j.extension.spring.ResolvableExpressionEvaluator;
import cn.crane4j.extension.spring.SpringAssembleAnnotationResolver;
import cn.crane4j.extension.spring.SpringParameterNameFinder;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAspect;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import cn.crane4j.spring.boot.annotation.EnableCrane4j;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * <p>The automatic configuration class of crane.<br />
 * Inject the current configuration class into the Spring container,
 * or make it effective directly through {@link EnableCrane4j} annotation,
 * which will automatically assemble various components required by the crane runtime.
 *
 * @author huangchengxing
 * @see cn.crane4j.extension.spring
 */
@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@EnableConfigurationProperties(Crane4jAutoConfiguration.Properties.class)
public class Crane4jAutoConfiguration {

    public static final String CRANE_PREFIX = "crane4j";

    // ============== basic components ==============

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        Crane4jApplicationContext context = new Crane4jApplicationContext(applicationContext);
        applicationContext.getBeansOfType(ContainerRegisterAware.class)
            .values().forEach(context::addContainerRegisterAware);
        return context;
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyOperator propertyOperator(Properties properties) {
        PropertyOperator operator = properties.isEnableAsmReflect() ?
            new AsmReflectPropertyOperator() : new ReflectPropertyOperator();
        if (properties.isEnableMapOperate()) {
            operator = new MapAccessiblePropertyOperator(operator);
        }
        if (properties.isEnableChainOperate()) {
            operator = new ChainAccessiblePropertyOperator(operator);
        }
        return operator;
    }

    @Bean
    @ConditionalOnMissingBean
    public MergedAnnotationFinder mergedAnnotationFinder() {
        return new MergedAnnotationFinder();
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleTypeResolver simpleTypeResolver() {
        return new SimpleTypeResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpelExpressionEvaluator spelExpressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentMapCacheManager concurrentMapCacheManager() {
        return new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CacheManager.class)
    public DefaultCacheableContainerProcessor defaultCacheableContainerProcessor(CacheManager cacheManager, Properties properties) {
        Map<String, String> containerConfigs = new HashMap<>(16);
        properties.getCacheContainers().forEach((cacheName, namespaces) ->
            namespaces.forEach(namespace -> containerConfigs.put(namespace, cacheName))
        );
        return new DefaultCacheableContainerProcessor(cacheManager, containerConfigs);
    }

    @Bean
    @ConditionalOnMissingBean
    public ContainerRegisteredLogger containerRegisteredLogger() {
        return new ContainerRegisteredLogger();
    }

    // ============== execute components ==============

    @Bean
    @ConditionalOnMissingBean
    public BeanFactoryResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringAssembleAnnotationResolver springAssembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver, Properties properties) {
        SpringAssembleAnnotationResolver resolver = new SpringAssembleAnnotationResolver(
            annotationFinder, configuration, evaluator, beanResolver
        );
        resolver.setLazyLoadAssembleContainer(properties.isLazyLoadAssembleContainer());
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public DisassembleAnnotationResolver disassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationResolver(annotationFinder, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public AssembleEnumAnnotationResolver assembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ConfigurableContainerProvider containerProvider) {
        return new AssembleEnumAnnotationResolver(annotationFinder, globalConfiguration, propertyOperator, containerProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeHierarchyBeanOperationParser typeHierarchyBeanOperationParser(Collection<OperationAnnotationResolver> resolvers) {
        return new TypeHierarchyBeanOperationParser(resolvers);
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public DisorderedBeanOperationExecutor disorderedBeanOperationExecutor() {
        return new DisorderedBeanOperationExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public OrderedBeanOperationExecutor orderedBeanOperationExecutor() {
        return new OrderedBeanOperationExecutor(Comparator.comparing(AssembleOperation::getSort));
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodInvokerContainerCreator methodInvokerContainerCreator(PropertyOperator propertyOperator) {
        return new MethodInvokerContainerCreator(propertyOperator);
    }

    @Order
    @Bean
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(methodInvokerContainerCreator, annotationFinder);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    @ConditionalOnBean(CacheManager.class)
    public CacheableMethodContainerFactory cacheableMethodContainerFactory(
        CacheManager cacheManager, MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new CacheableMethodContainerFactory(methodInvokerContainerCreator, annotationFinder, cacheManager);
    }

    @Primary
    @Bean
    public OneToOneReflexAssembleOperationHandler oneToOneReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToOneReflexAssembleOperationHandler(propertyOperator);
    }

    @Bean
    public ManyToManyReflexAssembleOperationHandler manyToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new ManyToManyReflexAssembleOperationHandler(propertyOperator);
    }

    @Bean
    public OneToManyReflexAssembleOperationHandler oneToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToManyReflexAssembleOperationHandler(propertyOperator);
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public ReflectDisassembleOperationHandler reflectDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectDisassembleOperationHandler(propertyOperator);
    }

    // ============== container provider ==============

    @Bean
    @ConditionalOnMissingBean
    public ThreadContextContainerProvider threadContextContainerProvider() {
        return new ThreadContextContainerProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public SharedContextContainerProvider sharedContextContainerProvider() {
        return new SharedContextContainerProvider();
    }

    // ============== operator interface components ==============

    @Bean
    @ConditionalOnMissingBean
    public DynamicSourceProxyMethodFactory dynamicSourceProxyMethodFactory(
        AnnotationFinder annotationFinder, ParameterNameFinder parameterNameFinder,
        ThreadContextContainerProvider provider, Properties properties) {
        return new DynamicSourceProxyMethodFactory(
            annotationFinder, parameterNameFinder, provider, properties.isClearContextAfterInvoke()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultProxyMethodFactory defaultProxyMethodFactory() {
        return new DefaultProxyMethodFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperatorProxyFactory operatorProxyFactory(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        Collection<OperatorProxyFactory.ProxyMethodFactory> factories) {
        return new OperatorProxyFactory(configuration, annotationFinder, factories);
    }

    // ============== extension components ==============

    @Bean
    @ConditionalOnMissingBean
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, DisorderedBeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringParameterNameFinder springParameterNameFinder() {
        return new SpringParameterNameFinder(new DefaultParameterNameDiscoverer());
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoOperateAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(Crane4jGlobalConfiguration crane4jGlobalConfiguration) {
        return new AutoOperateAnnotatedElementResolver(crane4jGlobalConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResolvableExpressionEvaluator resolvableExpressionEvaluator(
        ExpressionEvaluator expressionEvaluator, ParameterNameFinder parameterNameFinder, BeanResolver beanResolver) {
        return new ResolvableExpressionEvaluator(
            parameterNameFinder, expressionEvaluator, method -> {
                SpelExpressionContext context = new SpelExpressionContext(method);
                context.setBeanResolver(beanResolver);
                return context;
            }
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = CRANE_PREFIX,
        name = "enable-method-result-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    public MethodResultAutoOperateAspect methodResultAutoOperateAspect(
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAspect(autoOperateAnnotatedElementResolver, resolvableExpressionEvaluator);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = CRANE_PREFIX,
        name = "enable-method-argument-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    public MethodArgumentAutoOperateAspect methodArgumentAutoOperateAspect(
        MethodBaseExpressionExecuteDelegate methodBaseExpressionExecuteDelegate,
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ParameterNameFinder parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        return new MethodArgumentAutoOperateAspect(autoOperateAnnotatedElementResolver,
            methodBaseExpressionExecuteDelegate,
            parameterNameDiscoverer, annotationFinder
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = CRANE_PREFIX,
        name = "enable-method-container",
        havingValue = "true", matchIfMissing = true
    )
    public BeanMethodContainerRegistrar beanMethodContainerPostProcessor(
        AnnotationFinder annotationFinder, Collection<MethodContainerFactory> factories, Crane4jGlobalConfiguration configuration) {
        return new BeanMethodContainerRegistrar(factories, annotationFinder, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public Crane4jInitializer crane4jInitializer(
        MetadataReaderFactory readerFactory, ResourcePatternResolver resolver, PropertyOperator propertyOperator,
        ApplicationContext applicationContext, AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration configuration, Properties properties) {
        return new Crane4jInitializer(
            readerFactory, resolver, propertyOperator, applicationContext, properties, annotationFinder, configuration
        );
    }

    @Bean("Crane4jAutoConfigurationInitializationLogger")
    public InitializationLogger initializationLogger() {
        return new InitializationLogger();
    }

    /**
     * Configurable properties.
     *
     * @author huangchengxing
     * @see Crane4jInitializer
     */
    @ConfigurationProperties(prefix = CRANE_PREFIX)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Properties {

        /**
         * <p>Whether to enable objects of type {@link Map} be used as target objects or data source objects.<br />
         * <b><NOTE</b>:If the customized {@link PropertyOperator} is registered, the configuration will be overwritten.
         *
         * @see MapAccessiblePropertyOperator
         */
        private boolean enableMapOperate = true;

        /**
         * <p>Whether to enable accessing object properties through chain operators.<br />
         * <b><NOTE</b>:If the customized {@link PropertyOperator} is registered, the configuration will be overwritten.
         *
         * @see MapAccessiblePropertyOperator
         */
        private boolean enableChainOperate = true;

        /**
         * <p>Whether to enable reflection enhancement based on {@link com.esotericsoftware.reflectasm}.
         * Enabling can improve performance to some extent, but may increase additional memory usage.<br />
         * <b><NOTE</b>:If the customized {@link PropertyOperator} is registered, the configuration will be overwritten.
         *
         * @see AsmReflectPropertyOperator
         */
        private boolean enableAsmReflect = false;

        /**
         * <p>Scan the specified package path, adapt the enumeration
         * under the path and register it as a data source container.<br />
         * For example: {@code com.example.instant.enum.*}.
         *
         * <p>If only need to scan the enumeration annotated by
         * {@link ContainerEnum}, set {@link #onlyLoadAnnotatedEnum} is {@code true}.
         *
         * @see ContainerEnum
         * @see ConstantContainer#forEnum
         */
        private Set<String> containerEnumPackages = new LinkedHashSet<>();

        /**
         * Whether to load only the enumeration annotated by {@link ContainerEnum}.
         */
        private boolean onlyLoadAnnotatedEnum = false;

        /**
         * <p>Scan the specified package path, adapt the constant class annotated by
         * {@link ContainerConstant} under the path and register it as a data source container.<br />
         * For example: {@code com.example.instant.enum.*}.
         *
         * @see ContainerConstant
         * @see ConstantContainer#forConstantClass
         */
        private Set<String> containerConstantPackages = new LinkedHashSet<>();

        /**
         * <p>Scan all classes under the specified package path and pre-parse them
         * using the configuration parser in the spring context.<br />
         * For example: {@code com.example.entity.*}.
         *
         * <p>This configuration is conducive to improving the efficiency
         * of some configuration parsers with cache function.
         *
         * @see BeanOperationParser
         */
        private Set<String> operateEntityPackages = new LinkedHashSet<>();

        /**
         * Whether to enable automatic filling of aspect with method parameters.
         *
         * @see MethodArgumentAutoOperateAspect
         */
        private boolean enableMethodArgumentAutoOperate = true;

        /**
         * Whether to enable the method return value to automatically fill the cut surface.
         *
         * @see MethodResultAutoOperateAspect
         */
        private boolean enableMethodResultAutoOperate = true;

        /**
         * Whether to automatically scan and register the method
         * annotated by {@link ContainerMethod} as the data source container.
         *
         * @see BeanMethodContainerRegistrar
         */
        private boolean enableMethodContainer = true;

        /**
         * Declare which data sources need to be packaged as caches in the format {@code cache name: namespace of container}.
         */
        private Map<String, Set<String>> cacheContainers = new LinkedHashMap<>();

        /**
         * Whether allow delayed loading of containers during assembly operations.
         *
         * @see AssembleAnnotationResolver#setLazyLoadAssembleContainer
         * @see AssembleMpAnnotationResolver#setLazyLoadAssembleContainer
         */
        private boolean lazyLoadAssembleContainer = true;

        /**
         * Whether to clear the context after the method is invoked.
         *
         * @see DynamicSourceProxyMethodFactory
         */
        private boolean clearContextAfterInvoke = true;
    }

    /**
     * The default initializer is used to initialize some caches or components after the application is started.
     *
     * @author huangchengxing
     */
    @Slf4j
    @RequiredArgsConstructor
    public static class Crane4jInitializer implements ApplicationRunner {

        private final MetadataReaderFactory readerFactory;
        private final ResourcePatternResolver resolver;

        private final PropertyOperator propertyOperator;
        private final ApplicationContext applicationContext;
        private final Properties properties;
        private final AnnotationFinder annotationFinder;
        private final Crane4jGlobalConfiguration configuration;

        @SneakyThrows
        @Override
        public void run(ApplicationArguments args) {
            log.info("start initializing crane4j components......");
            // load enumeration and register it as a container
            loadContainerEnum();
            // load a constant class and register it as a container
            loadConstantClass();
            // pre resolution class operation configuration
            loadOperateEntity();
        }

        private void loadConstantClass() {
            Set<String> constantPackages = properties.getContainerConstantPackages();
            constantPackages.forEach(path -> readMetadata(path, reader -> {
                Class<?> targetType = ClassUtil.loadClass(reader.getClassMetadata().getClassName());
                if (AnnotatedElementUtils.isAnnotated(targetType, ContainerConstant.class)) {
                    Container<?> container = ConstantContainer.forConstantClass(targetType, annotationFinder);
                    configuration.registerContainer(container);
                }
            }));
        }

        @SuppressWarnings("unchecked")
        private void loadContainerEnum() {
            Set<String> enumPackages = properties.getContainerEnumPackages();
            enumPackages.forEach(path -> readMetadata(path, reader -> {
                Class<?> targetType = ClassUtil.loadClass(reader.getClassMetadata().getClassName());
                boolean supported = targetType.isEnum()
                    && (!properties.isOnlyLoadAnnotatedEnum() || AnnotatedElementUtils.isAnnotated(targetType, ContainerEnum.class));
                if (supported) {
                    Container<Enum<?>> container = ConstantContainer.forEnum((Class<Enum<?>>)targetType, annotationFinder, propertyOperator);
                    configuration.registerContainer(container);
                }
            }));
        }

        private void loadOperateEntity() {
            Set<String> entityPackages = properties.getOperateEntityPackages();
            entityPackages.forEach(path -> readMetadata(path, reader -> {
                Class<?> targetType = ClassUtil.loadClass(reader.getClassMetadata()
                    .getClassName());
                applicationContext.getBeansOfType(BeanOperationParser.class).values()
                    .forEach(parser -> parser.parse(targetType));
            }));
        }

        @SneakyThrows
        private void readMetadata(String path, Consumer<MetadataReader> consumer) {
            String actualPath = CharSequenceUtil.replace(path, ".", "/");
            Resource[] resources = resolver.getResources(actualPath);
            for (Resource resource : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                consumer.accept(reader);
            }
        }
    }

    /**
     * Initialization logger.
     *
     * @author huangchengxing
     */
    @Slf4j
    public static class InitializationLogger implements ApplicationRunner {
        @Override
        public void run(ApplicationArguments args) {
            log.info("Initialized crane4j components......");
        }
    }
}
