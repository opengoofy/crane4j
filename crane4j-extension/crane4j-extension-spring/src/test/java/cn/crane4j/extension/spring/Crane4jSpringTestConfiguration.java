package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.ConfigurableContainerProvider;
import cn.crane4j.core.container.SharedContextContainerProvider;
import cn.crane4j.core.container.ThreadContextContainerProvider;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
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
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.support.operator.DefaultProxyMethodFactory;
import cn.crane4j.core.support.operator.DynamicSourceProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAspect;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author huangchengxing
 */
@EnableAspectJAutoProxy
@Configuration
public class Crane4jSpringTestConfiguration {

    @Primary
    @Bean
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        List<ContainerRegisterAware> awareList = applicationContext
            .getBeanNamesForType(ContainerRegisterAware.class).length > 0 ?
            new ArrayList<>(applicationContext.getBeansOfType(ContainerRegisterAware.class).values()) : new ArrayList<>();
        Crane4jApplicationContext context = new Crane4jApplicationContext(applicationContext);
        awareList.forEach(context::addContainerRegisterAware);
        return context;
    }

    @Bean
    public PropertyOperator propertyOperator() {
        PropertyOperator operator = new ReflectPropertyOperator();
        operator = new MapAccessiblePropertyOperator(operator);
        return new ChainAccessiblePropertyOperator(operator);
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

    @Bean
    public SpringParameterNameFinder springParameterNameFinder() {
        return new SpringParameterNameFinder(new DefaultParameterNameDiscoverer());
    }

    @Bean
    public ContainerRegisteredLogger containerRegisteredLogger() {
        return new ContainerRegisteredLogger();
    }

    // ============== execute components ==============

    @Bean
    public BeanFactoryResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Bean
    public SpringAssembleAnnotationResolver springAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver) {
        return new SpringAssembleAnnotationResolver(annotationFinder, configuration, evaluator, beanResolver);
    }

    @Bean
    public DisassembleAnnotationResolver disassembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationResolver(annotationFinder, configuration);
    }

    @Bean
    public AssembleEnumAnnotationResolver assembleEnumAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator, ConfigurableContainerProvider containerProvider) {
        return new AssembleEnumAnnotationResolver(annotationFinder, globalConfiguration, propertyOperator, containerProvider);
    }

    @Primary
    @Bean
    public TypeHierarchyBeanOperationParser typeHierarchyBeanOperationParser(Collection<OperationAnnotationResolver> operationAnnotationResolver) {
        return new TypeHierarchyBeanOperationParser(operationAnnotationResolver);
    }

    @Primary
    @Bean
    public DisorderedBeanOperationExecutor disorderedBeanOperationExecutor() {
        return new DisorderedBeanOperationExecutor();
    }

    @Bean
    public OrderedBeanOperationExecutor orderedBeanOperationExecutor() {
        return new OrderedBeanOperationExecutor(Comparator.comparing(AssembleOperation::getSort));
    }

    @Bean
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
    public ReflectDisassembleOperationHandler reflectDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectDisassembleOperationHandler(propertyOperator);
    }

    // ============== container provider ==============

    @Bean
    public ThreadContextContainerProvider threadContextContainerProvider() {
        return new ThreadContextContainerProvider();
    }

    @Bean
    public SharedContextContainerProvider sharedContextContainerProvider() {
        return new SharedContextContainerProvider();
    }

    // ============== operator interface components ==============

    @Bean
    public DynamicSourceProxyMethodFactory dynamicSourceProxyMethodFactory(
        AnnotationFinder annotationFinder, ParameterNameFinder parameterNameFinder,
        ThreadContextContainerProvider dynamicSourceContainerProvider) {
        return new DynamicSourceProxyMethodFactory(
            annotationFinder, parameterNameFinder, dynamicSourceContainerProvider, true
        );
    }

    @Bean
    public DefaultProxyMethodFactory defaultProxyMethodFactory() {
        return new DefaultProxyMethodFactory();
    }

    @Bean
    public OperatorProxyFactory operatorProxyFactory(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        Collection<OperatorProxyFactory.ProxyMethodFactory> factories) {
        return new OperatorProxyFactory(configuration, annotationFinder, factories);
    }

    // ============== extension components ==============

    @Bean
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, DisorderedBeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    public AutoOperateAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(Crane4jGlobalConfiguration crane4jGlobalConfiguration) {
        return new AutoOperateAnnotatedElementResolver(crane4jGlobalConfiguration);
    }

    @Bean
    public ResolvableExpressionEvaluator resolvableExpressionEvaluator(
        ExpressionEvaluator expressionEvaluator, ParameterNameFinder parameterNameDiscoverer, BeanResolver beanResolver) {
        return new ResolvableExpressionEvaluator(
            parameterNameDiscoverer, expressionEvaluator,
            method -> {
                SpelExpressionContext context = new SpelExpressionContext();
                context.setBeanResolver(beanResolver);
                return context;
            }
        );
    }

    @Bean
    public MethodResultAutoOperateAspect methodResultAutoOperateAspect(
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAspect(autoOperateAnnotatedElementResolver, resolvableExpressionEvaluator);
    }

    @Bean
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
    public BeanMethodContainerRegistrar beanMethodContainerPostProcessor(
        Collection<MethodContainerFactory> factories, AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new BeanMethodContainerRegistrar(factories, annotationFinder, configuration);
    }
}
