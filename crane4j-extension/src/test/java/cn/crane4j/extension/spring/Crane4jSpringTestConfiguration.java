package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperationsResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.aop.AutoOperateMethodAnnotatedElementResolver;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.callback.ContainerRegisteredLogger;
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
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
import org.springframework.core.ParameterNameDiscoverer;
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
        return new Crane4jApplicationContext(applicationContext, awareList);
    }

    @Bean
    public PropertyOperator propertyOperator() {
        PropertyOperator operator = new ReflectPropertyOperator();
        operator = new MapAccessiblePropertyOperator(operator);
        return new ChainAccessiblePropertyOperator(operator);
    }

    @Bean
    public AnnotationFinder annotationFinder() {
        return new MergedAnnotationFinder();
    }

    @Bean
    public TypeResolver typeResolver() {
        return new SimpleTypeResolver();
    }

    @Bean
    public ExpressionEvaluator expressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);
    }

    @Bean
    public ContainerRegisteredLogger containerRegisteredLogger() {
        return new ContainerRegisteredLogger();
    }

    // ============== execute components ==============

    @Bean
    public BeanResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Primary
    @Bean
    public SpringAnnotationOperationsResolver springAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver) {
        return new SpringAnnotationOperationsResolver(annotationFinder, configuration, evaluator, beanResolver);
    }

    @Primary
    @Bean
    public BeanOperationParser typeHierarchyBeanOperationParser(Collection<BeanOperationsResolver> beanOperationsResolver) {
        return new TypeHierarchyBeanOperationParser(beanOperationsResolver);
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

    @Order
    @Bean
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        PropertyOperator propertyOperator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(propertyOperator, annotationFinder);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
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
    public ReflectDisassembleOperationHandler reflectDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectDisassembleOperationHandler(propertyOperator);
    }

    // ============== extension components ==============

    @Bean
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, DisorderedBeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @Bean
    public AutoOperateMethodAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver(Crane4jGlobalConfiguration crane4jGlobalConfiguration) {
        return new AutoOperateMethodAnnotatedElementResolver(crane4jGlobalConfiguration);
    }

    @Bean
    public ResolvableExpressionEvaluator resolvableExpressionEvaluator(
        ExpressionEvaluator expressionEvaluator, ParameterNameDiscoverer parameterNameDiscoverer, BeanResolver beanResolver) {
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
        AutoOperateMethodAnnotatedElementResolver autoOperateMethodAnnotatedElementResolver,
        ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        return new MethodResultAutoOperateAspect(autoOperateMethodAnnotatedElementResolver, resolvableExpressionEvaluator);
    }

    @Bean
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
    public MergedAnnotationMethodContainerPostProcessor mergedAnnotationMethodContainerPostProcessor(
        Collection<MethodContainerFactory> factories, Crane4jGlobalConfiguration configuration) {
        return new MergedAnnotationMethodContainerPostProcessor(factories, configuration);
    }
}
