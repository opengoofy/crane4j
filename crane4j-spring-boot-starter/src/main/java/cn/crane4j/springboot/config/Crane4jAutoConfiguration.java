package cn.crane4j.springboot.config;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCache;
import cn.crane4j.core.cache.SimpleCacheManager;
import cn.crane4j.core.container.CacheableMethodContainerFactory;
import cn.crane4j.core.container.DefaultMethodContainerFactory;
import cn.crane4j.core.container.MethodContainerFactory;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ReflectAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.crane4j.springboot.annotation.EnableCrane4j;
import cn.crane4j.springboot.support.AnnotationMethodContainerProcessor;
import cn.crane4j.springboot.support.Crane4jApplicationContext;
import cn.crane4j.springboot.support.MergedAnnotationFinder;
import cn.crane4j.springboot.support.MethodBaseExpressionEvaluator;
import cn.crane4j.springboot.support.OperateTemplate;
import cn.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import cn.crane4j.springboot.support.expression.SpelExpressionContext;
import cn.crane4j.springboot.support.expression.SpelExpressionEvaluator;
import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>crane的自动配置类。将当前配置类注入Spring容器，或者直接通过{@link EnableCrane4j}使其生效，
 * 将自动装配crane运行时所需的各个组件。
 *
 * @author huangchengxing
 */
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@EnableConfigurationProperties(Crane4jProperties.class)
public class Crane4jAutoConfiguration {

    // ============== 基础组件 ==============

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        return new SimpleCacheManager(
            new ConcurrentHashMap<>(8),
            cacheName -> new ConcurrentMapCache<>(new ConcurrentHashMap<>(16))
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        return new Crane4jApplicationContext(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyOperator propertyOperator(Crane4jProperties crane4jProperties) {
        return crane4jProperties.isEnableAsmReflect() ?
            new AsmReflectPropertyOperator() : new ReflectPropertyOperator();
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

    // ============== 操作解析器 ==============

    @Bean
    @ConditionalOnMissingBean
    public AnnotationAwareBeanOperationParser annotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new AnnotationAwareBeanOperationParser(annotationFinder, configuration);
    }

    // ============== 操作执行器 ==============

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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MethodAccess.class)
    public ReflectAssembleOperationHandler reflectAssembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectAssembleOperationHandler(propertyOperator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReflectDisassembleOperationHandler reflectDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectDisassembleOperationHandler(propertyOperator);
    }

    // ============== 扩展组件 ==============

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
        ApplicationContext applicationContext, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator) {
        return new MethodResultAutoOperateAspect(applicationContext, methodBaseExpressionEvaluator);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = Crane4jProperties.CRANE_PREFIX,
        name = "enable-method-argument-auto-operate",
        havingValue = "true", matchIfMissing = true
    )
    public MethodArgumentAutoOperateAspect methodArgumentAutoOperateAspect(
        ApplicationContext applicationContext, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator, ParameterNameDiscoverer parameterNameDiscoverer) {
        return new MethodArgumentAutoOperateAspect(applicationContext, methodBaseExpressionEvaluator, parameterNameDiscoverer);
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
