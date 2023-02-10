package cn.createsequence.crane4j.springboot.config;

import cn.createsequence.crane4j.core.container.DefaultMethodContainerFactory;
import cn.createsequence.crane4j.core.container.MethodContainerFactory;
import cn.createsequence.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.createsequence.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.createsequence.crane4j.core.executor.handler.ReflectAssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.createsequence.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.createsequence.crane4j.core.parser.AssembleOperation;
import cn.createsequence.crane4j.core.support.AnnotationFinder;
import cn.createsequence.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.createsequence.crane4j.core.support.SimpleTypeResolver;
import cn.createsequence.crane4j.core.support.TypeResolver;
import cn.createsequence.crane4j.core.support.expression.ExpressionEvaluator;
import cn.createsequence.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.createsequence.crane4j.core.support.reflect.PropertyOperator;
import cn.createsequence.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.createsequence.crane4j.springboot.annotation.EnableCrane4j;
import cn.createsequence.crane4j.springboot.support.AnnotationMethodContainerProcessor;
import cn.createsequence.crane4j.springboot.support.Crane4jApplicationContext;
import cn.createsequence.crane4j.springboot.support.MergedAnnotationFinder;
import cn.createsequence.crane4j.springboot.support.MethodBaseExpressionEvaluator;
import cn.createsequence.crane4j.springboot.support.OperateTemplate;
import cn.createsequence.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.createsequence.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import cn.createsequence.crane4j.springboot.support.expression.SpelExpressionContext;
import cn.createsequence.crane4j.springboot.support.expression.SpelExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collection;
import java.util.Comparator;

/**
 * <p>crane的自动配置类。将当前配置类注入Spring容器，或者直接通过{@link EnableCrane4j}使其生效，
 * 将自动装配crane运行时所需的各个组件。
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
@EnableConfigurationProperties(Crane4jProperties.class)
public class Crane4jAutoConfiguration {

    // ============== 基础组件 ==============

    @Bean
    @ConditionalOnMissingBean
    public Crane4jGlobalConfiguration craneGlobalConfiguration(ApplicationContext applicationContext) {
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

    @Bean
    @ConditionalOnMissingBean
    public DefaultMethodContainerFactory defaultMethodContainerFactory(
        PropertyOperator propertyOperator, AnnotationFinder annotationFinder) {
        return new DefaultMethodContainerFactory(propertyOperator, annotationFinder);
    }

    @Bean
    @ConditionalOnMissingBean
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
    public MethodBaseExpressionEvaluator methodBaseExpressionEvaluator(
        ApplicationContext applicationContext, ExpressionEvaluator expressionEvaluator) {
        return new MethodBaseExpressionEvaluator(
            new DefaultParameterNameDiscoverer(), expressionEvaluator,
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
        ApplicationContext applicationContext, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator) {
        return new MethodArgumentAutoOperateAspect(applicationContext, methodBaseExpressionEvaluator);
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
