package cn.crane4j.spring.boot.config;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperationsResolver;
import cn.crane4j.core.parser.DisassembleAnnotationOperationsResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.aop.AutoOperateMethodAnnotatedElementResolver;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.callback.ContainerRegisteredLogger;
import cn.crane4j.core.support.callback.DefaultCacheableContainerProcessor;
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.Crane4jApplicationContext;
import cn.crane4j.extension.spring.MergedAnnotationFinder;
import cn.crane4j.extension.spring.MergedAnnotationMethodContainerPostProcessor;
import cn.crane4j.extension.spring.ResolvableExpressionEvaluator;
import cn.crane4j.extension.spring.SpringAssembleAnnotationOperationsResolver;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAspect;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import cn.crane4j.spring.boot.annotation.EnableCrane4j;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>The automatic configuration class of crane.<br />
 * Inject the current configuration class into the Spring container,
 * or make it effective directly through {@link EnableCrane4j} annotation,
 * which will automatically assemble various components required by the crane runtime.
 *
 * @author huangchengxing
 * @see cn.crane4j.extension.spring
 */
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
    public AnnotationFinder annotationFinder() {
        return new MergedAnnotationFinder();
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeResolver typeResolver() {
        return new SimpleTypeResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpressionEvaluator expressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CacheManager.class)
    public DefaultCacheableContainerProcessor cacheableContainerRegistrar(CacheManager cacheManager, Properties properties) {
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
    public BeanResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringAssembleAnnotationOperationsResolver springAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver) {
        return new SpringAssembleAnnotationOperationsResolver(annotationFinder, configuration, evaluator, beanResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public DisassembleAnnotationOperationsResolver disassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationOperationsResolver(annotationFinder, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanOperationParser typeHierarchyBeanOperationParser(Collection<BeanOperationsResolver> resolvers) {
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

    @Order
    @Bean
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        PropertyOperator propertyOperator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(propertyOperator, annotationFinder);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    @ConditionalOnBean(CacheManager.class)
    public CacheableMethodContainerFactory cacheableMethodContainerFactory(
        CacheManager cacheManager, PropertyOperator propertyOperator, AnnotationFinder annotationFinder) {
        return new CacheableMethodContainerFactory(propertyOperator, annotationFinder, cacheManager);
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

    // ============== extension components ==============

    @Bean
    @ConditionalOnMissingBean
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, DisorderedBeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoOperateMethodAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(Crane4jGlobalConfiguration crane4jGlobalConfiguration) {
        return new AutoOperateMethodAnnotatedElementResolver(crane4jGlobalConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResolvableExpressionEvaluator resolvableExpressionEvaluator(
        ExpressionEvaluator expressionEvaluator, ParameterNameDiscoverer parameterNameDiscoverer, BeanResolver beanResolver) {
        return new ResolvableExpressionEvaluator(
            parameterNameDiscoverer, expressionEvaluator, method -> {
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
        AutoOperateMethodAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAspect(autoOperateMethodAnnotatedElementResolver, resolvableExpressionEvaluator);
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
        AutoOperateMethodAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver,
        ParameterNameDiscoverer parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        return new MethodArgumentAutoOperateAspect(
            autoOperateMethodAnnotatedElementResolver,
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
    public MergedAnnotationMethodContainerPostProcessor mergedAnnotationMethodContainerPostProcessor(
        Collection<MethodContainerFactory> factories, Crane4jGlobalConfiguration configuration) {
        return new MergedAnnotationMethodContainerPostProcessor(factories, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public Crane4jInitializer crane4jInitializer(
        MetadataReaderFactory readerFactory, ResourcePatternResolver resolver, ApplicationContext applicationContext,
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration, Properties properties) {
        return new Crane4jInitializer(
            readerFactory, resolver, applicationContext, properties, annotationFinder, configuration
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
    @Data
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
         * @see MergedAnnotationMethodContainerPostProcessor
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

        private final MetadataReaderFactory readerFactory;
        private final ResourcePatternResolver resolver;

        private final ApplicationContext applicationContext;
        private final Properties properties;
        private final AnnotationFinder annotationFinder;
        private final Crane4jGlobalConfiguration configuration;

        @SneakyThrows
        @Override
        public void run(ApplicationArguments args) {
            log.info("start initializing crane4j components......");
            // load bean operations resolver
            loadBeanOperationsResolver();
            // load enumeration and register it as a container
            loadContainerEnum();
            // load a constant class and register it as a container
            loadConstantClass();
            // pre resolution class operation configuration
            loadOperateEntity();
        }

        public void loadBeanOperationsResolver() {
            String[] parserNames = applicationContext.getBeanNamesForType(TypeHierarchyBeanOperationParser.class);
            String[] resolverNames = applicationContext.getBeanNamesForType(BeanOperationsResolver.class);
            if (ArrayUtil.isNotEmpty(parserNames) && ArrayUtil.isNotEmpty(resolverNames)) {
                List<BeanOperationsResolver> resolvers = Stream.of(resolverNames)
                    .map(beanName -> applicationContext.getBean(beanName, BeanOperationsResolver.class))
                    .collect(Collectors.toList());
                Stream.of(parserNames)
                    .map(beanName -> applicationContext.getBean(beanName, TypeHierarchyBeanOperationParser.class))
                    .forEach(parser -> resolvers.forEach(parser::addBeanOperationsResolver));
            }
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
                    Container<Enum<?>> container = ConstantContainer.forEnum((Class<Enum<?>>)targetType, annotationFinder);
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
