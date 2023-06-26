package cn.crane4j.spring.boot.config;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.lifecycle.CacheableContainerProcessor;
import cn.crane4j.core.container.lifecycle.ContainerInstanceLifecycleProcessor;
import cn.crane4j.core.container.lifecycle.ContainerRegisterLogger;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.handler.AssembleEnumAnnotationHandler;
import cn.crane4j.core.parser.handler.DisassembleAnnotationHandler;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.support.operator.DefaultOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.DynamicContainerOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.operator.OperatorProxyMethodFactory;
import cn.crane4j.core.support.reflect.AsmReflectivePropertyOperator;
import cn.crane4j.core.support.reflect.CacheablePropertyOperator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.BeanMethodContainerRegistrar;
import cn.crane4j.extension.spring.Crane4jApplicationContext;
import cn.crane4j.extension.spring.MergedAnnotationFinder;
import cn.crane4j.extension.spring.ResolvableExpressionEvaluator;
import cn.crane4j.extension.spring.SpringAssembleAnnotationHandler;
import cn.crane4j.extension.spring.SpringParameterNameFinder;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAdvisor;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAdvisor;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import cn.crane4j.spring.boot.annotation.EnableCrane4j;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
@EnableConfigurationProperties(Crane4jAutoConfiguration.Properties.class)
public class Crane4jAutoConfiguration {

    public static final String CRANE_PREFIX = "crane4j";

    // ============== basic components ==============

    @ConditionalOnMissingBean
    @Bean({"hutoolConverterRegister", "HutoolConverterManager"})
    public HutoolConverterManager hutoolConverterRegister() {
        return new HutoolConverterManager();
    }

    @Primary
    @ConditionalOnMissingBean
    @Bean({"crane4jApplicationContext", "Crane4jApplicationContext"})
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        return new Crane4jApplicationContext(applicationContext);
    }

    @Bean({"PropertyOperator", "propertyOperator"})
    public PropertyOperator propertyOperator(Properties properties, ConverterManager converterManager) {
        PropertyOperator operator = properties.isEnableAsmReflect() ?
            new AsmReflectivePropertyOperator(converterManager) : new ReflectivePropertyOperator(converterManager);
        operator = new CacheablePropertyOperator(operator);
        if (properties.isEnableMapOperate()) {
            operator = new MapAccessiblePropertyOperator(operator);
        }
        if (properties.isEnableChainOperate()) {
            operator = new ChainAccessiblePropertyOperator(operator);
        }
        return operator;
    }

    @ConditionalOnMissingBean
    @Bean({"MergedAnnotationFinder", "mergedAnnotationFinder"})
    public MergedAnnotationFinder mergedAnnotationFinder() {
        return new MergedAnnotationFinder();
    }

    @ConditionalOnMissingBean
    @Bean({"SimpleTypeResolver", "simpleTypeResolver"})
    public SimpleTypeResolver simpleTypeResolver() {
        return new SimpleTypeResolver();
    }

    @ConditionalOnMissingBean
    @Bean({"SpelExpressionEvaluator", "spelExpressionEvaluator"})
    public SpelExpressionEvaluator spelExpressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @ConditionalOnMissingBean
    @Bean({"ConcurrentMapCacheManager", "concurrentMapCacheManager"})
    public ConcurrentMapCacheManager concurrentMapCacheManager() {
        return new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);
    }

    @Order(0)
    @Bean({"ContainerInstanceLifecycleProcessor", "containerInstanceLifecycleProcessor"})
    public ContainerInstanceLifecycleProcessor containerInstanceLifecycleProcessor() {
        return new ContainerInstanceLifecycleProcessor();
    }

    @Order(1)
    @Bean({"ContainerRegisterLogger", "containerRegisterLogger"})
    public ContainerRegisterLogger containerRegisterLogger() {
        Logger logger = LoggerFactory.getLogger(ContainerRegisterLogger.class);
        return new ContainerRegisterLogger(logger::info);
    }

    @Order(2)
    @ConditionalOnMissingBean
    @ConditionalOnBean(CacheManager.class)
    @Bean({"CacheableContainerProcessor", "cacheableContainerProcessor"})
    public CacheableContainerProcessor cacheableContainerProcessor(CacheManager cacheManager, Properties properties) {
        Map<String, String> cacheMap = new HashMap<>(16);
        properties.getCacheContainers().forEach((cacheName, namespaces) ->
            namespaces.forEach(namespace -> cacheMap.put(namespace, cacheName))
        );
        CacheableContainerProcessor processor = new CacheableContainerProcessor(cacheManager);
        processor.setCacheNameSelector((definition, container) -> cacheMap.get(container.getNamespace()));
        return processor;
    }

    // ============== execute components ==============

    @ConditionalOnMissingBean
    @Bean({"BeanFactoryResolver", "beanFactoryResolver"})
    public BeanFactoryResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @ConditionalOnMissingBean
    @Bean({"SpringAssembleAnnotationHandler", "springAssembleAnnotationResolver"})
    public SpringAssembleAnnotationHandler springAssembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver, Properties properties) {
        return new SpringAssembleAnnotationHandler(
            annotationFinder, configuration, evaluator, beanResolver
        );
    }

    @ConditionalOnMissingBean
    @Bean({"DisassembleAnnotationHandler", "disassembleAnnotationOperationsResolver"})
    public DisassembleAnnotationHandler disassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationHandler(annotationFinder, configuration);
    }

    @ConditionalOnMissingBean
    @Bean({"AssembleEnumAnnotationHandler", "assembleEnumAnnotationResolver"})
    public AssembleEnumAnnotationHandler assembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ContainerManager containerManager) {
        return new AssembleEnumAnnotationHandler(annotationFinder, globalConfiguration, propertyOperator, containerManager);
    }

    @ConditionalOnMissingBean
    @Bean({"TypeHierarchyBeanOperationParser", "typeHierarchyBeanOperationParser"})
    public TypeHierarchyBeanOperationParser typeHierarchyBeanOperationParser(Collection<OperationAnnotationHandler> resolvers) {
        return new TypeHierarchyBeanOperationParser(resolvers);
    }

    @Primary
    @ConditionalOnMissingBean
    @Bean({"DisorderedBeanOperationExecutor", "disorderedBeanOperationExecutor"})
    public DisorderedBeanOperationExecutor disorderedBeanOperationExecutor(ContainerManager containerManager) {
        return new DisorderedBeanOperationExecutor(containerManager);
    }

    @ConditionalOnMissingBean
    @Bean({"OrderedBeanOperationExecutor", "orderedBeanOperationExecutor"})
    public OrderedBeanOperationExecutor orderedBeanOperationExecutor(ContainerManager containerManager) {
        return new OrderedBeanOperationExecutor(containerManager, Comparator.comparing(AssembleOperation::getSort));
    }

    @ConditionalOnMissingBean
    @Bean({"MethodInvokerContainerCreator", "methodInvokerContainerCreator"})
    public MethodInvokerContainerCreator methodInvokerContainerCreator(PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new MethodInvokerContainerCreator(propertyOperator, converterManager);
    }

    @Order
    @Bean({"DefaultMethodContainerFactory", "defaultMethodContainerFactory"})
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(methodInvokerContainerCreator, annotationFinder);
    }

    @Order
    @Bean({"DynamicContainerOperatorProxyMethodFactory", "dynamicContainerOperatorProxyMethodFactory"})
    public DynamicContainerOperatorProxyMethodFactory dynamicContainerOperatorProxyMethodFactory(
        ConverterManager converterManager, ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        return new DynamicContainerOperatorProxyMethodFactory(converterManager, parameterNameFinder, annotationFinder);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @ConditionalOnBean(CacheManager.class)
    @Bean({"CacheableMethodContainerFactory", "cacheableMethodContainerFactory"})
    public CacheableMethodContainerFactory cacheableMethodContainerFactory(
        CacheManager cacheManager, MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new CacheableMethodContainerFactory(methodInvokerContainerCreator, annotationFinder, cacheManager);
    }

    @Primary
    @Bean({"OneToOneAssembleOperationHandler", "oneToOneReflexAssembleOperationHandler"})
    public OneToOneAssembleOperationHandler oneToOneReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToOneAssembleOperationHandler(propertyOperator);
    }

    @Bean({"ManyToManyAssembleOperationHandler", "manyToManyReflexAssembleOperationHandler"})
    public ManyToManyAssembleOperationHandler manyToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new ManyToManyAssembleOperationHandler(propertyOperator);
    }

    @Bean({"OneToManyAssembleOperationHandler", "oneToManyReflexAssembleOperationHandler"})
    public OneToManyAssembleOperationHandler oneToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToManyAssembleOperationHandler(propertyOperator);
    }

    @Primary
    @ConditionalOnMissingBean
    @Bean({"ReflectiveDisassembleOperationHandler", "reflectiveDisassembleOperationHandler"})
    public ReflectiveDisassembleOperationHandler reflectiveDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectiveDisassembleOperationHandler(propertyOperator);
    }

    // ============== operator interface components ==============

    @ConditionalOnMissingBean
    @Bean({"DefaultProxyMethodFactory", "defaultProxyMethodFactory"})
    public DefaultOperatorProxyMethodFactory defaultProxyMethodFactory(ConverterManager converterManager) {
        return new DefaultOperatorProxyMethodFactory(converterManager);
    }

    @ConditionalOnMissingBean
    @Bean({"OperatorProxyFactory", "operatorProxyFactory"})
    public OperatorProxyFactory operatorProxyFactory(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        Collection<OperatorProxyMethodFactory> factories) {
        OperatorProxyFactory factory = new OperatorProxyFactory(configuration, annotationFinder);
        factories.forEach(factory::addProxyMethodFactory);
        return factory;
    }

    // ============== extension components ==============

    @ConditionalOnMissingBean
    @Bean({"OperateTemplate", "operateTemplate"})
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, BeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @ConditionalOnMissingBean
    @Bean({"SpringParameterNameFinder", "springParameterNameFinder"})
    public SpringParameterNameFinder springParameterNameFinder() {
        return new SpringParameterNameFinder(new DefaultParameterNameDiscoverer());
    }

    @ConditionalOnMissingBean
    @Bean({"AutoOperateAnnotatedElementResolver", "autoOperateMethodAnnotatedElementResolver"})
    public AutoOperateAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(Crane4jGlobalConfiguration crane4jGlobalConfiguration) {
        return new AutoOperateAnnotatedElementResolver(crane4jGlobalConfiguration);
    }

    @ConditionalOnMissingBean
    @Bean({"ResolvableExpressionEvaluator", "resolvableExpressionEvaluator"})
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

    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = CRANE_PREFIX,
        name = "enable-method-result-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    @Bean({"MethodResultAutoOperateAdvisor", "methodResultAutoOperateAdvisor"})
    public MethodResultAutoOperateAdvisor methodResultAutoOperateAdvisor(
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAdvisor(autoOperateAnnotatedElementResolver, resolvableExpressionEvaluator);
    }

    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = CRANE_PREFIX,
        name = "enable-method-argument-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    @Bean({"MethodArgumentAutoOperateAdvisor", "methodArgumentAutoOperateAdvisor"})
    public MethodArgumentAutoOperateAdvisor methodArgumentAutoOperateAdvisor(
        MethodBaseExpressionExecuteDelegate methodBaseExpressionExecuteDelegate,
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ParameterNameFinder parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        return new MethodArgumentAutoOperateAdvisor(autoOperateAnnotatedElementResolver,
            methodBaseExpressionExecuteDelegate,
            parameterNameDiscoverer, annotationFinder
        );
    }

    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = CRANE_PREFIX,
        name = "enable-method-container",
        havingValue = "true", matchIfMissing = true
    )
    @Bean({"BeanMethodContainerRegistrar", "beanMethodContainerPostProcessor"})
    public BeanMethodContainerRegistrar beanMethodContainerPostProcessor(
        AnnotationFinder annotationFinder, Collection<MethodContainerFactory> factories, Crane4jGlobalConfiguration configuration) {
        return new BeanMethodContainerRegistrar(factories, annotationFinder, configuration);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @ConditionalOnMissingBean
    @Bean({"Crane4jInitializer", "crane4jInitializer"})
    public Crane4jInitializer crane4jInitializer(
        MetadataReaderFactory readerFactory, ResourcePatternResolver resolver, PropertyOperator propertyOperator,
        ApplicationContext applicationContext, AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration configuration, Properties properties) {
        return new Crane4jInitializer(
            readerFactory, resolver, propertyOperator, applicationContext, properties, annotationFinder, configuration
        );
    }

    @Bean({"InitializationLogger", "initializationLogger"})
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
         * @see AsmReflectivePropertyOperator
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
         * @see MethodArgumentAutoOperateAdvisor
         */
        private boolean enableMethodArgumentAutoOperate = true;

        /**
         * Whether to enable the method return value to automatically fill the cut surface.
         *
         * @see MethodResultAutoOperateAdvisor
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
    }

    /**
     * The default initializer is used to initialize some caches or components after the application is started.
     *
     * @author huangchengxing
     */
    @Slf4j
    @RequiredArgsConstructor
    public static class Crane4jInitializer implements ApplicationRunner {

        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
                Class<?> targetType = ClassUtils.forName(reader.getClassMetadata().getClassName());
                if (AnnotatedElementUtils.isAnnotated(targetType, ContainerConstant.class)) {
                    Container<Object> container = ConstantContainer.forConstantClass(targetType, annotationFinder);
                    configuration.registerContainer(container);
                }
            }));
        }

        @SuppressWarnings("unchecked")
        private void loadContainerEnum() {
            Set<String> enumPackages = properties.getContainerEnumPackages();
            enumPackages.forEach(path -> readMetadata(path, reader -> {
                Class<?> targetType = ClassUtils.forName(reader.getClassMetadata().getClassName());
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
                Class<?> targetType = ClassUtils.forName(reader.getClassMetadata().getClassName());
                applicationContext.getBeansOfType(BeanOperationParser.class).values()
                    .forEach(parser -> parser.parse(targetType));
            }));
        }

        @SneakyThrows
        private void readMetadata(String path, Consumer<MetadataReader> consumer) {
            String actualPath = ClassUtils.packageToPath(path);
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
