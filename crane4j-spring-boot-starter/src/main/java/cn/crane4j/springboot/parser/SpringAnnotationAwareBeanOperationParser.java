package cn.crane4j.springboot.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.springboot.support.expression.SpelExpressionContext;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * <p>Extension implementation of {@link SpringAnnotationAwareBeanOperationParser},
 * On the basis of the former, some spring annotations are additionally supported.
 * <ul>
 *     <li>support to sort operations according to the rules of Spring {@link Order} annotation priority;</li>
 *     <li>supports obtaining containers through SpEL expressions;</li>
 * </ul>
 *
 * @author huangchengxing
 * @see Order
 */
@Slf4j
public class SpringAnnotationAwareBeanOperationParser extends AnnotationAwareBeanOperationParser {

    private final ExpressionEvaluator evaluator;
    private final BeanFactory beanFactory;

    /**
     * <p>Create an operation parser that supports annotation configuration.<br />
     * The order of operation configurations is {@link Order#value()} or {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration global configuration
     */
    public SpringAnnotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        ExpressionEvaluator evaluator, BeanFactory beanFactory) {
        super(annotationFinder, globalConfiguration, Sorted.comparator());
        this.evaluator = evaluator;
        this.beanFactory = beanFactory;
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
        provider = ObjectUtil.defaultIfNull(provider, globalConfiguration);

        // determine container by expression
        Container<?> container = null;
        try {
            container = getContainerByExpression(annotation.container(), provider);
        } catch (Exception e) {
            // maybe namespace is not an expression? ignore it and take it again
        }

        // get container directly if parser cannot determine by expression
        if (Objects.isNull(container)) {
            container = provider.getContainer(annotation.container());
        }
        Assert.notNull(
            container, throwException("cannot find container [{}] from provider [{}]", annotation.container(), provider.getClass())
        );
        return container;
    }

    /**
     * Parse {@link Assemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Assemble}
     */
    @Override
    protected List<Assemble> parseAssembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(
            beanType, Assemble.class, SpringAnnotationAwareBeanOperationParser::processAnnotation
        );
    }

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     */
    @Override
    protected List<Disassemble> parseDisassembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(
            beanType, Disassemble.class, SpringAnnotationAwareBeanOperationParser::processAnnotation
        );
    }

    private Container<?> getContainerByExpression(String expression, ContainerProvider provider) {
        Object target = null;
        try {
            SpelExpressionContext context = new SpelExpressionContext();
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
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

    private static <T extends Annotation> T processAnnotation(T a, Field f) {
        // force value to be set to the annotated attribute name
        ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
        // force sort if field annotated by @Order
        Order annotation = AnnotatedElementUtils.findMergedAnnotation(f, Order.class);
        if (Objects.nonNull(annotation)) {
            ReflectUtils.setAttributeValue(a, "sort", annotation.value());
        }
        return a;
    }
}
