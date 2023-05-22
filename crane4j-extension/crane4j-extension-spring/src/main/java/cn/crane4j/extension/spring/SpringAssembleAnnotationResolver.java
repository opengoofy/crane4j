package cn.crane4j.extension.spring;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.parser.AssembleAnnotationResolver;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.*;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.BeanResolver;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>Extension implementation of {@link AssembleAnnotationResolver},
 * On the basis of the former, some spring annotations are additionally supported.
 * <ul>
 *     <li>support to sort operations according to the rules of Spring {@link Order} annotation priority;</li>
 *     <li>supports obtaining containers through SpEL expressions;</li>
 * </ul>
 *
 * @author huangchengxing
 * @see Order
 * @since 1.2.0
 */
@Slf4j
public class SpringAssembleAnnotationResolver
    extends AssembleAnnotationResolver implements EmbeddedValueResolverAware {

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
    public SpringAssembleAnnotationResolver(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver) {
        super(annotationFinder, globalConfiguration, Sorted.comparator());
        this.evaluator = evaluator;
        this.beanResolver = beanResolver;
    }

    /**
     * Parse annotation for fields
     *
     * @param beanType bean type
     * @return element and annotation map
     */
    @Override
    protected MultiMap<AnnotatedElement, Assemble> parseAnnotationForFields(Class<?> beanType) {
        MultiMap<AnnotatedElement, Assemble> result = super.parseAnnotationForFields(beanType);
        result.forEach((e, a) -> Optional
            .ofNullable(AnnotatedElementUtils.findMergedAnnotation(e, Order.class))
            .ifPresent(o -> ReflectUtils.setAttributeValue(a, "sort", o.value()))
        );
        return result;
    }

    /**
     * Get container.
     *
     * @param annotation annotation
     * @return container
     * @throws IllegalArgumentException thrown when the container is null
     */
    @Override
    protected Container<?> getContainer(Assemble annotation) {
        // determine provider
        ContainerProvider provider = ConfigurationUtil.getContainerProvider(
            globalConfiguration, annotation.containerProviderName(), annotation.containerProvider()
        );
        provider = ObjectUtils.defaultIfNull(provider, globalConfiguration);

        // determine container by expression
        Container<?> container = null;
        String namespace = stringValueResolver.resolveStringValue(annotation.container());
        try {
            container = getContainerByExpression(namespace, provider);
        } catch (Exception e) {
            // maybe namespace is not an expression? ignore it and take it again
        }

        // get container directly if parser cannot determine by expression
        if (Objects.isNull(container)) {
            container = provider.getContainer(namespace);
        }
        Asserts.isNotNull(container, "cannot find container [{}] from provider [{}]", annotation.container(), annotation.containerProvider());
        return container;
    }

    private Container<?> getContainerByExpression(String expression, ContainerProvider provider) {
        Object target = null;
        try {
            SpelExpressionContext context = new SpelExpressionContext();
            context.setBeanResolver(beanResolver);
            context.setVariable("provider", provider);
            target = evaluator.execute(expression, Object.class, context);
        } catch (Exception e) {
            log.debug("cannot resolve container or namespace of container from expression [{}]", expression);
        }
        if (target instanceof Container) {
            return (Container<?>)target;
        }
        if (target instanceof String) {
            return provider.getContainer((String)target);
        }
        return null;
    }

    /**
     * Set the StringValueResolver to use for resolving embedded definition values.
     *
     * @param resolver resolver
     */
    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }
}
