package cn.crane4j.extension.spring;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.handler.AssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.StringUtils;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.expression.BeanResolver;
import org.springframework.util.StringValueResolver;

import java.util.Objects;

/**
 * <p>Extension implementation of {@link AssembleAnnotationHandler},
 * On the basis of the former, some spring annotations are additionally supported.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
@Slf4j
public class ValueResolveAssembleAnnotationHandler
    extends AssembleAnnotationHandler implements EmbeddedValueResolverAware {

    private final ExpressionEvaluator evaluator;
    private final BeanResolver beanResolver;
    @Setter
    private StringValueResolver embeddedValueResolver;

    /**
     * <p>Create an operation parser that supports annotation configuration.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration global configuration
     * @param beanResolver       bean resolver
     * @param evaluator          expression evaluator
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public ValueResolveAssembleAnnotationHandler(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(annotationFinder, globalConfiguration, Crane4jGlobalSorter.comparator(), propertyMappingStrategyManager);
        this.evaluator = evaluator;
        this.beanResolver = beanResolver;
    }

    /**
     * Get container.
     *
     * @param annotation annotation
     * @return namespace of container
     * @throws IllegalArgumentException thrown when the container is null
     */
    @Override
    protected String getContainerNamespace(Assemble annotation) {
        String namespace = resolveNamespace(annotation.container());
        if (StringUtils.isEmpty(namespace)) {
            return Container.EMPTY_CONTAINER_NAMESPACE;
        }
        String providerName = resolveNamespace(annotation.containerProvider());
        return ContainerManager.canonicalNamespace(namespace, providerName);
    }

    @Nullable
    private String resolveNamespace(String expression) {
        String namespace = Objects.isNull(embeddedValueResolver) ?
                expression : embeddedValueResolver.resolveStringValue(expression);
        try {
            SpelExpressionContext context = new SpelExpressionContext();
            context.setBeanResolver(beanResolver);
            namespace = evaluator.execute(expression, String.class, context);
        } catch (Exception e) {
            log.debug("cannot resolve container or namespace of container from expression [{}]", expression);
        }
        return namespace;
    }
}
