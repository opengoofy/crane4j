package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.ContainerManager;
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
import cn.crane4j.core.parser.handler.strategy.OverwriteMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.OverwriteNotNullMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.ReferenceMappingStrategy;
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
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.support.operator.DefaultOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.DynamicContainerOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.operator.OperatorProxyMethodFactory;
import cn.crane4j.core.support.reflect.CacheablePropertyOperator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperatorHolder;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAdvisor;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAdvisor;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import cn.crane4j.extension.spring.scanner.ClassScanner;
import cn.crane4j.extension.spring.scanner.ScannedContainerRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collection;
import java.util.Comparator;

/**
 * Default configuration class for crane4j.
 *
 * @author huangchengxing
 */
@Slf4j
@EnableAspectJAutoProxy
public class DefaultCrane4jSpringConfiguration implements SmartInitializingSingleton {

    // ============== basic components ==============

    @Bean
    public ClassScanner classScanner() {
        return ClassScanner.INSTANCE;
    }

    @Bean
    public SpringConverterManager springConverterManager() {
        return new SpringConverterManager(DefaultConversionService.getSharedInstance());
    }

    @Primary
    @Bean
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        return new Crane4jApplicationContext(applicationContext);
    }

    @Bean
    public ScannedContainerRegistrar scannedContainerRegister() {
        return new ScannedContainerRegistrar();
    }

    @Bean
    public PropertyOperator propertyOperator(ConverterManager converterManager) {
        PropertyOperator operator = new ReflectivePropertyOperator(converterManager);
        operator = new CacheablePropertyOperator(operator);
        operator = new MapAccessiblePropertyOperator(operator);
        operator = new ChainAccessiblePropertyOperator(operator);
        return new PropertyOperatorHolder(operator);
    }

    @Bean
    public MergedAnnotationFinder mergedAnnotationFinder() {
        return new MergedAnnotationFinder();
    }

    @Bean
    public SimpleTypeResolver simpleTypeResolver() {
        return new SimpleTypeResolver();
    }

    @Bean
    public SpelExpressionEvaluator spelExpressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @Bean
    public ConcurrentMapCacheManager concurrentMapCacheManager() {
        return new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);
    }

    @Order(0)
    @Bean
    public ContainerInstanceLifecycleProcessor containerInstanceLifecycleProcessor() {
        return new ContainerInstanceLifecycleProcessor();
    }

    @Order(1)
    @Bean
    public ContainerRegisterLogger containerRegisterLogger() {
        Logger logger = LoggerFactory.getLogger(ContainerRegisterLogger.class);
        return new ContainerRegisterLogger(logger::info);
    }

    // ============== execute components ==============

    @Bean
    public OverwriteNotNullMappingStrategy overwriteNotNullMappingStrategy() {
        return OverwriteNotNullMappingStrategy.INSTANCE;
    }

    @Bean
    public OverwriteMappingStrategy overwriteMappingStrategy() {
        return OverwriteMappingStrategy.INSTANCE;
    }

    @Bean
    public ReferenceMappingStrategy referenceMappingStrategy(PropertyOperator propertyOperator) {
        return new ReferenceMappingStrategy(propertyOperator);
    }

    @Bean
    public BeanFactoryResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Bean
    public SpringAssembleAnnotationHandler springAssembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver,
        Collection<PropertyMappingStrategy> propertyMappingStrategies) {
        SpringAssembleAnnotationHandler handler = new SpringAssembleAnnotationHandler(
            annotationFinder, configuration, evaluator, beanResolver
        );
        propertyMappingStrategies.forEach(handler::addPropertyMappingStrategy);
        return handler;
    }

    @Bean
    public AssembleEnumAnnotationHandler assembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ContainerManager containerManager,
        Collection<PropertyMappingStrategy> propertyMappingStrategies) {
        AssembleEnumAnnotationHandler handler = new AssembleEnumAnnotationHandler(annotationFinder, globalConfiguration, propertyOperator, containerManager);
        propertyMappingStrategies.forEach(handler::addPropertyMappingStrategy);
        return handler;
    }

    @Bean
    public DisassembleAnnotationHandler disassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationHandler(annotationFinder, configuration);
    }

    @Bean
    public TypeHierarchyBeanOperationParser typeHierarchyBeanOperationParser(Collection<OperationAnnotationHandler> resolvers) {
        TypeHierarchyBeanOperationParser parser =  new TypeHierarchyBeanOperationParser();
        resolvers.forEach(parser::addOperationAnnotationHandler);
        return parser;
    }

    @Primary
    @Bean
    public DisorderedBeanOperationExecutor disorderedBeanOperationExecutor(ContainerManager containerManager) {
        return new DisorderedBeanOperationExecutor(containerManager);
    }

    @Bean
    public OrderedBeanOperationExecutor orderedBeanOperationExecutor(ContainerManager containerManager) {
        return new OrderedBeanOperationExecutor(containerManager, Comparator.comparing(AssembleOperation::getSort));
    }

    @Bean
    public MethodInvokerContainerCreator methodInvokerContainerCreator(PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new MethodInvokerContainerCreator(propertyOperator, converterManager);
    }

    @Order
    @Bean
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(methodInvokerContainerCreator, annotationFinder);
    }

    @Order
    @Bean
    public DynamicContainerOperatorProxyMethodFactory dynamicContainerOperatorProxyMethodFactory(
        ConverterManager converterManager, ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        return new DynamicContainerOperatorProxyMethodFactory(converterManager, parameterNameFinder, annotationFinder);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    public CacheableMethodContainerFactory cacheableMethodContainerFactory(
        CacheManager cacheManager, MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new CacheableMethodContainerFactory(methodInvokerContainerCreator, annotationFinder, cacheManager);
    }

    @Primary
    @Bean
    public OneToOneAssembleOperationHandler oneToOneAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToOneAssembleOperationHandler(propertyOperator);
    }

    @Bean
    public ManyToManyAssembleOperationHandler manyToManyAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new ManyToManyAssembleOperationHandler(propertyOperator);
    }

    @Bean
    public OneToManyAssembleOperationHandler oneToManyAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToManyAssembleOperationHandler(propertyOperator);
    }

    @Primary
    @Bean
    public ReflectiveDisassembleOperationHandler reflectiveDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectiveDisassembleOperationHandler(propertyOperator);
    }

    // ============== operator interface components ==============

    @Bean
    public DefaultOperatorProxyMethodFactory defaultProxyMethodFactory(ConverterManager converterManager) {
        return new DefaultOperatorProxyMethodFactory(converterManager);
    }

    @Bean
    public OperatorProxyFactory operatorProxyFactory(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        Collection<OperatorProxyMethodFactory> factories) {
        OperatorProxyFactory proxyFactory = new OperatorProxyFactory(configuration, annotationFinder);
        factories.forEach(proxyFactory::addProxyMethodFactory);
        return proxyFactory;
    }

    // ============== extension components ==============

    @Bean
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, BeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    public SpringParameterNameFinder springParameterNameFinder() {
        return new SpringParameterNameFinder(new DefaultParameterNameDiscoverer());
    }

    @Bean
    public AutoOperateAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(
        Crane4jGlobalConfiguration crane4jGlobalConfiguration, TypeResolver typeResolver) {
        return new AutoOperateAnnotatedElementResolver(crane4jGlobalConfiguration, typeResolver);
    }

    @Bean
    public ResolvableExpressionEvaluator resolvableExpressionEvaluator(
        ExpressionEvaluator expressionEvaluator, ParameterNameFinder parameterNameFinder, BeanResolver beanResolver) {
        return new ResolvableExpressionEvaluator(
            parameterNameFinder, expressionEvaluator, method -> {
            SpelExpressionContext context = new SpelExpressionContext(method);
            context.setBeanResolver(beanResolver);
            return context;
        });
    }

    @Bean
    public MethodResultAutoOperateAdvisor methodResultAutoOperateAdvisor(
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAdvisor(autoOperateAnnotatedElementResolver, resolvableExpressionEvaluator);
    }

    @Bean
    public MethodArgumentAutoOperateAdvisor methodArgumentAutoOperateAdvisor(
        MethodBaseExpressionExecuteDelegate methodBaseExpressionExecuteDelegate,
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ParameterNameFinder parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        return new MethodArgumentAutoOperateAdvisor(autoOperateAnnotatedElementResolver,
            methodBaseExpressionExecuteDelegate,
            parameterNameDiscoverer, annotationFinder
        );
    }

    @Bean
    public BeanMethodContainerRegistrar beanMethodContainerRegistrar(
        AnnotationFinder annotationFinder, Collection<MethodContainerFactory> factories, Crane4jGlobalConfiguration configuration) {
        return new BeanMethodContainerRegistrar(factories, annotationFinder, configuration);
    }

    /**
     * Invoked right at the end of the singleton pre-instantiation phase,
     * with a guarantee that all regular singleton beans have been created
     * already. {@link ListableBeanFactory#getBeansOfType} calls within
     * this method won't trigger accidental side effects during bootstrap.
     * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
     * lazily initialized on demand after {@link BeanFactory} bootstrap,
     * and not for any other bean scope either. Carefully use it for beans
     * with the intended bootstrap semantics only.
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.info("Initialized crane4j components......");
    }
}
