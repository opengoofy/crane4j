package cn.crane4j.core.support.auto;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>An handler that resolve the {@link AutoOperate}
 * annotation on the {@link Method} or {@link Parameter} to {@link AutoOperateAnnotatedElement}.
 *
 * @author huangchengxing
 * @see AutoOperate
 * @since 2.3.0
 */
public class MethodBasedAutoOperateAnnotatedElementResolver extends AbstractAutoOperateAnnotatedElementResolver {

    /**
     * Type resolver, which is used to resolve the type of the annotated element
     * when the type is not specified in the {@link AutoOperate} annotation.
     */
    private final TypeResolver typeResolver;

    /**
     * Create {@link MethodBasedAutoOperateAnnotatedElementResolver} with {@link Crane4jGlobalConfiguration}.
     *
     * @param configuration configuration
     * @param typeResolver type resolver
     */
    public MethodBasedAutoOperateAnnotatedElementResolver(
        Crane4jGlobalConfiguration configuration, TypeResolver typeResolver) {
        super(configuration);
        this.typeResolver = typeResolver;
    }

    /**
     * Whether the resolver supports the element.
     *
     * @param element    element
     * @param annotation annotation
     * @return true if supports, otherwise false
     */
    @Override
    public boolean support(AnnotatedElement element, @Nullable AutoOperate annotation) {
        return element instanceof Method || element instanceof Parameter;
    }

    /**
     * Create {@link AutoOperateAnnotatedElement} for the element.
     *
     * @param element    element
     * @param annotation annotation
     * @param parser     parser
     * @param executor   executor
     * @param extractor  extractor
     * @param filter     filter
     * @return {@link AutoOperateAnnotatedElement}
     */
    @Nullable
    @Override
    protected AutoOperateAnnotatedElement createAutoOperateAnnotatedElement(
        AnnotatedElement element, AutoOperate annotation,
        BeanOperationParser parser, BeanOperationExecutor executor,
        MethodInvoker extractor, Predicate<? super KeyTriggerOperation> filter) {
        if (!support(element, annotation)) {
            return null;
        }

        // type is specified in annotation
        Class<?> annotationSpecifiedType = annotation.type();
        if (!ClassUtils.isObjectOrVoid(annotationSpecifiedType)) {
            BeanOperations beanOperations = parser.parse(annotation.type());
            return beanOperations.isEmpty() ?
                AutoOperateAnnotatedElement.EMPTY : DefaultAutoOperateAnnotatedElement.forStaticTypeOperation(
                annotation, element, extractor, filter, beanOperations, executor
            );
        }

        // not specify type, delay parsing until execution time
        Function<Object, BeanOperations> dynamicParser = t -> Optional.ofNullable(t)
            .map(typeResolver::resolve)
            .map(parser::parse)
            .orElse(BeanOperations.empty());
        return DefaultAutoOperateAnnotatedElement.forDynamicTypeOperation(
            annotation, element, extractor, filter, executor, dynamicParser
        );
    }

    @Override
    protected MethodInvoker resolveExtractor(AnnotatedElement element, AutoOperate annotation) {
        Class<?> type = resolveTypeForExtractor(element);
        String on = annotation.on();
        MethodInvoker extractor = (t, args) -> t;
        if (StringUtils.isNotEmpty(on)) {
            PropertyOperator propertyOperator = configuration.getPropertyOperator();
            extractor = propertyOperator.findGetter(type, on);
            Asserts.isNotNull(extractor, "cannot find getter for [{}] on [{}]", on, annotation.type());
        }
        return extractor;
    }

    private static Class<?> resolveTypeForExtractor(AnnotatedElement element) {
        if (element instanceof Method) {
            return ((Method)element).getReturnType();
        } else if (element instanceof Parameter) {
            return ((Parameter)element).getType();
        } else {
            throw new Crane4jException("cannot resolve type for annotated element [{}]", element);
        }
    }
}
