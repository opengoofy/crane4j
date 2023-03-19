package cn.crane4j.springboot.config;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.CacheableMethodContainerFactory;
import cn.crane4j.core.container.DefaultMethodContainerFactory;
import cn.crane4j.core.container.MethodContainerFactory;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneReflexAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ContainerRegisteredLogger;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.crane4j.core.support.callback.DefaultCacheableContainerProcessor;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.springboot.annotation.EnableCrane4j;
import cn.crane4j.springboot.parser.SpringAnnotationAwareBeanOperationParser;
import cn.crane4j.springboot.support.AnnotationMethodContainerProcessor;
import cn.crane4j.springboot.support.Crane4jApplicationContext;
import cn.crane4j.springboot.support.MergedAnnotationFinder;
import cn.crane4j.springboot.support.MethodBaseExpressionEvaluator;
import cn.crane4j.springboot.support.OperateTemplate;
import cn.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import cn.crane4j.springboot.support.expression.SpelExpressionContext;
import cn.crane4j.springboot.support.expression.SpelExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The automatic configuration class of crane.<br />
 * Inject the current configuration class into the Spring container,
 * or make it effective directly through {@link EnableCrane4j} annotation,
 * which will automatically assemble various components required by the crane runtime.
 *
 * @author huangchengxing
 */
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@EnableConfigurationProperties(Crane4jProperties.class)
public class Crane4jAutoConfiguration {

    // ============== basic components ==============

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        List<ContainerRegisterAware> awareList = applicationContext
            .getBeanNamesForType(ContainerRegisterAware.class).length > 0 ?
            new ArrayList<>(applicationContext.getBeansOfType(ContainerRegisterAware.class).values()) : new ArrayList<>();
        return new Crane4jApplicationContext(applicationContext, awareList);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyOperator propertyOperator(Crane4jProperties crane4jProperties) {
        PropertyOperator operator = crane4jProperties.isEnableAsmReflect() ?
            new AsmReflectPropertyOperator() : new ReflectPropertyOperator();
        return new MapAccessiblePropertyOperator(operator);
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
    public DefaultCacheableContainerProcessor cacheableContainerRegistrar(CacheManager cacheManager, Crane4jProperties crane4jProperties) {
        Map<String, String> containerConfigs = new HashMap<>(16);
        crane4jProperties.getCacheContainers().forEach((cacheName, namespaces) ->
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

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public SpringAnnotationAwareBeanOperationParser springAnnotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, ApplicationContext applicationContext) {
        return new SpringAnnotationAwareBeanOperationParser(annotationFinder, configuration, evaluator, applicationContext);
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
    @ConditionalOnMissingBean
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        PropertyOperator propertyOperator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(propertyOperator, annotationFinder);
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CacheManager.class)
    public CacheableMethodContainerFactory cacheableMethodContainerFactory(
        CacheManager cacheManager, PropertyOperator propertyOperator, AnnotationFinder annotationFinder) {
        return new CacheableMethodContainerFactory(propertyOperator, annotationFinder, cacheManager);
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public OneToOneReflexAssembleOperationHandler oneToOneReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new OneToOneReflexAssembleOperationHandler(propertyOperator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ManyToManyReflexAssembleOperationHandler manyToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new ManyToManyReflexAssembleOperationHandler(propertyOperator);
    }

    @Bean
    @ConditionalOnMissingBean
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
        AnnotationAwareBeanOperationParser parser, DisorderedBeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodBaseExpressionEvaluator methodBaseExpressionEvaluator(
        ApplicationContext applicationContext, ExpressionEvaluator expressionEvaluator, ParameterNameDiscoverer parameterNameDiscoverer) {
        return new MethodBaseExpressionEvaluator(
            parameterNameDiscoverer, expressionEvaluator,
            method -> {
                SpelExpressionContext context = new SpelExpressionContext();
                context.setBeanResolver(new BeanFactoryResolver(applicationContext));
                return context;
            }
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = Crane4jProperties.CRANE_PREFIX,
        name = "enable-method-result-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    public MethodResultAutoOperateAspect methodResultAutoOperateAspect(
        Crane4jGlobalConfiguration configuration, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator) {
        return new MethodResultAutoOperateAspect(configuration, methodBaseExpressionEvaluator);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = Crane4jProperties.CRANE_PREFIX,
        name = "enable-method-argument-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    public MethodArgumentAutoOperateAspect methodArgumentAutoOperateAspect(
        Crane4jGlobalConfiguration configuration, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator, ParameterNameDiscoverer parameterNameDiscoverer) {
        return new MethodArgumentAutoOperateAspect(configuration, methodBaseExpressionEvaluator, parameterNameDiscoverer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = Crane4jProperties.CRANE_PREFIX,
        name = "enable-method-container",
        havingValue = "true", matchIfMissing = true
    )
    public AnnotationMethodContainerProcessor annotationMethodContainerProcessor(
        Collection<MethodContainerFactory> factories, Crane4jApplicationContext configuration) {
        return new AnnotationMethodContainerProcessor(factories, configuration);
    }
}
