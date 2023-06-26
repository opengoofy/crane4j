package cn.crane4j.extension.spring;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.handler.AssembleAnnotationHandler;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.StringUtils;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.Order;
import org.springframework.expression.BeanResolver;
import org.springframework.util.StringValueResolver;

import java.util.Objects;

/**
 * <p>Extension implementation of {@link AssembleAnnotationHandler},
 * On the basis of the former, some spring annotations are additionally supported.
 * <ul>
 *     <li>support to sort operations according to the rules of Spring {@link Order} annotation priority;</li>
 *     <li>supports obtaining containers through SpEL expressions;</li>
 * </ul>
 *
 * @author huangchengxing
 * @since 1.2.0
 */
@Slf4j
public class SpringAssembleAnnotationHandler
    extends AssembleAnnotationHandler implements EmbeddedValueResolverAware {

    private final ExpressionEvaluator evaluator;
    private final BeanResolver beanResolver;
    private StringValueResolver stringValueResolver;

    /**
     * <p>Create an operation parser that supports annotation configuration.<br />
     * The order of operation configurations is {@link Order#value()} or {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration global configuration
     */
    public SpringAssembleAnnotationHandler(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver) {
        super(annotationFinder, globalConfiguration, Crane4jGlobalSorter.comparator());
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
        String namespace = Objects.isNull(stringValueResolver) ?
                expression : stringValueResolver.resolveStringValue(expression);
        try {
            SpelExpressionContext context = new SpelExpressionContext();
            context.setBeanResolver(beanResolver);
            namespace = evaluator.execute(expression, String.class, context);
        } catch (Exception e) {
            log.debug("cannot resolve container or namespace of container from expression [{}]", expression);
        }
        return namespace;
    }

    /**
     * Set the StringValueResolver to use for resolving embedded definition values.
     *
     * @param resolver handler
     */
    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }
}
