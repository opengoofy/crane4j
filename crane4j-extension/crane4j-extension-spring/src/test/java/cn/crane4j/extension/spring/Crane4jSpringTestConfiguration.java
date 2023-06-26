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
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAdvisor;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAdvisor;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Configuration class for test cases.
 *
 * @author huangchengxing
 */
@EnableAspectJAutoProxy
@Configuration
public class Crane4jSpringTestConfiguration {

    // ============== basic components ==============

    @Bean({"hutoolConverterRegister", "HutoolConverterManager"})
    public HutoolConverterManager hutoolConverterRegister() {
        return new HutoolConverterManager();
    }

    @Primary
    @Bean({"crane4jApplicationContext", "Crane4jApplicationContext"})
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        return new Crane4jApplicationContext(applicationContext);
    }

    @Bean({"PropertyOperator", "propertyOperator"})
    public PropertyOperator propertyOperator(ConverterManager converterManager) {
        return Optional.of(new ReflectivePropertyOperator(converterManager))
                .map(ChainAccessiblePropertyOperator::new)
                .map(MapAccessiblePropertyOperator::new)
                .map(ChainAccessiblePropertyOperator::new)
                .get();
    }

    @Bean({"MergedAnnotationFinder", "mergedAnnotationFinder"})
    public MergedAnnotationFinder mergedAnnotationFinder() {
        return new MergedAnnotationFinder();
    }

    @Bean({"SimpleTypeResolver", "simpleTypeResolver"})
    public SimpleTypeResolver simpleTypeResolver() {
        return new SimpleTypeResolver();
    }

    @Bean({"SpelExpressionEvaluator", "spelExpressionEvaluator"})
    public SpelExpressionEvaluator spelExpressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

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

    // ============== execute components ==============

    @Bean({"BeanFactoryResolver", "beanFactoryResolver"})
    public BeanFactoryResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Bean({"SpringAssembleAnnotationHandler", "springAssembleAnnotationResolver"})
    public SpringAssembleAnnotationHandler springAssembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver) {
        return new SpringAssembleAnnotationHandler(
            annotationFinder, configuration, evaluator, beanResolver
        );
    }

    @Bean({"DisassembleAnnotationHandler", "disassembleAnnotationOperationsResolver"})
    public DisassembleAnnotationHandler disassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationHandler(annotationFinder, configuration);
    }

    @Bean({"AssembleEnumAnnotationHandler", "assembleEnumAnnotationResolver"})
    public AssembleEnumAnnotationHandler assembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ContainerManager containerManager) {
        return new AssembleEnumAnnotationHandler(annotationFinder, globalConfiguration, propertyOperator, containerManager);
    }

    @Bean({"TypeHierarchyBeanOperationParser", "typeHierarchyBeanOperationParser"})
    public TypeHierarchyBeanOperationParser typeHierarchyBeanOperationParser(Collection<OperationAnnotationHandler> resolvers) {
        return new TypeHierarchyBeanOperationParser(resolvers);
    }

    @Primary
    @Bean({"DisorderedBeanOperationExecutor", "disorderedBeanOperationExecutor"})
    public DisorderedBeanOperationExecutor disorderedBeanOperationExecutor(ContainerManager containerManager) {
        return new DisorderedBeanOperationExecutor(containerManager);
    }

    @Bean({"OrderedBeanOperationExecutor", "orderedBeanOperationExecutor"})
    public OrderedBeanOperationExecutor orderedBeanOperationExecutor(ContainerManager containerManager) {
        return new OrderedBeanOperationExecutor(containerManager, Comparator.comparing(AssembleOperation::getSort));
    }

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
    @Bean({"ReflectiveDisassembleOperationHandler", "reflectiveDisassembleOperationHandler"})
    public ReflectiveDisassembleOperationHandler reflectiveDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectiveDisassembleOperationHandler(propertyOperator);
    }

    // ============== operator interface components ==============

    @Bean({"DefaultProxyMethodFactory", "defaultProxyMethodFactory"})
    public DefaultOperatorProxyMethodFactory defaultProxyMethodFactory(ConverterManager converterManager) {
        return new DefaultOperatorProxyMethodFactory(converterManager);
    }

    @Bean({"OperatorProxyFactory", "operatorProxyFactory"})
    public OperatorProxyFactory operatorProxyFactory(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        Collection<OperatorProxyMethodFactory> factories) {
        OperatorProxyFactory proxyFactory = new OperatorProxyFactory(configuration, annotationFinder);
        factories.forEach(proxyFactory::addProxyMethodFactory);
        return proxyFactory;
    }

    // ============== extension components ==============

    @Bean({"OperateTemplate", "operateTemplate"})
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, BeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean({"SpringParameterNameFinder", "springParameterNameFinder"})
    public SpringParameterNameFinder springParameterNameFinder() {
        return new SpringParameterNameFinder(new DefaultParameterNameDiscoverer());
    }

    @Bean({"AutoOperateAnnotatedElementResolver", "autoOperateMethodAnnotatedElementResolver"})
    public AutoOperateAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(Crane4jGlobalConfiguration crane4jGlobalConfiguration) {
        return new AutoOperateAnnotatedElementResolver(crane4jGlobalConfiguration);
    }

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

    @Bean({"MethodResultAutoOperateAdvisor", "methodResultAutoOperateAdvisor"})
    public MethodResultAutoOperateAdvisor methodResultAutoOperateAdvisor(
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAdvisor(autoOperateAnnotatedElementResolver, resolvableExpressionEvaluator);
    }

    @Bean({"methodArgumentAutoOperateAdvisor", "methodArgumentAutoOperateAdvisor"})
    public MethodArgumentAutoOperateAdvisor methodArgumentAutoOperateAdvisor(
        MethodBaseExpressionExecuteDelegate methodBaseExpressionExecuteDelegate,
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ParameterNameFinder parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        return new MethodArgumentAutoOperateAdvisor(autoOperateAnnotatedElementResolver,
            methodBaseExpressionExecuteDelegate,
            parameterNameDiscoverer, annotationFinder
        );
    }

    @Bean({"BeanMethodContainerRegistrar", "beanMethodContainerRegistrar"})
    public BeanMethodContainerRegistrar beanMethodContainerRegistrar(
        AnnotationFinder annotationFinder, Collection<MethodContainerFactory> factories, Crane4jGlobalConfiguration configuration) {
        return new BeanMethodContainerRegistrar(factories, annotationFinder, configuration);
    }
}
