package cn.crane4j.core.support.auto;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>A handler that resolve the {@link AutoOperate} annotation
 * on the {@link Class} to {@link AutoOperateAnnotatedElement}.
 * 
 * <p>Compared to {@link MethodBasedAutoOperateAnnotatedElementResolver},
 * this resolver can only resolve the static type operation,
 * and support the condition expression.
 * 
 * @author huangchengxing
 * @since 2.3.0
 * @see AutoOperate
 * @see ExpressionEvaluator
 * @see ExpressionContext
 */
public class ClassBasedAutoOperateAnnotatedElementResolver extends AbstractAutoOperateAnnotatedElementResolver {

    private final ExpressionEvaluator expressionEvaluator;
    private final Function<Object, ExpressionContext> contextFactory;

    /**
     * Create {@link ClassBasedAutoOperateAnnotatedElementResolver} with {@link Crane4jGlobalConfiguration}.
     *
     * @param configuration configuration
     * @param expressionEvaluator expression evaluator
     * @param contextFactory context factory
     */
    public ClassBasedAutoOperateAnnotatedElementResolver(
        Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator expressionEvaluator, Function<Object, ExpressionContext> contextFactory) {
        super(configuration);
        this.expressionEvaluator = expressionEvaluator;
        this.contextFactory = contextFactory;
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
        return element instanceof Class;
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
    protected AutoOperateAnnotatedElement createAutoOperateAnnotatedElement(AnnotatedElement element, AutoOperate annotation, BeanOperationParser parser, BeanOperationExecutor executor, MethodInvoker extractor, Predicate<? super KeyTriggerOperation> filter) {
        if (!support(element, annotation)) {
            return null;
        }
        Class<?> type = ClassUtils.isObjectOrVoid(annotation.type()) ?
            (Class<?>)element : annotation.type();
        BeanOperations beanOperations = parser.parse(type);
        if (beanOperations.isEmpty()) {
            return null;
        }
        DefaultAutoOperateAnnotatedElement result = DefaultAutoOperateAnnotatedElement.forStaticTypeOperation(
            annotation, type, extractor, filter, beanOperations, executor
        );
        // if no condition, return directly
        return StringUtils.isEmpty(annotation.condition()) ?
            result : new ConditionalAutoOperateElement(result);
    }

    /**
     * Resolve the extractor for {@link AutoOperate#value()}.
     *
     * @param element    element
     * @param annotation annotation
     * @return extractor
     */
    @Override
    protected MethodInvoker resolveExtractor(AnnotatedElement element, AutoOperate annotation) {
        String on = annotation.on();
        if (StringUtils.isEmpty(on)) {
            return (t, args) -> t;
        }
        return (t, args) -> Optional.ofNullable(configuration.getPropertyOperator())
            .map(po -> po.findGetter(t.getClass(), on))
            .orElseThrow(() -> new Crane4jException("cannot find getter for [{}] on [{}]", on, t.getClass()))
            .invoke(t, args);
    }

    /**
     * A {@link AutoOperateAnnotatedElement} that can be filtered by condition.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    private class ConditionalAutoOperateElement implements AutoOperateAnnotatedElement {

        private final DefaultAutoOperateAnnotatedElement delegate;

        /**
         * get the {@link AutoOperate} annotation.
         *
         * @return annotation
         */
        @Override
        public AutoOperate getAnnotation() {
            return delegate.getAnnotation();
        }

        /**
         * Get the annotated element.
         *
         * @return element
         */
        @Override
        public AnnotatedElement getElement() {
            return delegate.getElement();
        }

        /**
         * <p>Get the {@link BeanOperations} for the annotated element.<br/>
         * If the resolver cannot determine the {@link BeanOperations} for the annotated element exactly,
         * it can return null, in this case, the {@link BeanOperations} will be resolved in the runtime.
         *
         * @return beanOperations
         */
        @Override
        public @Nullable BeanOperations getBeanOperations() {
            return delegate.getBeanOperations();
        }

        /**
         * Execute the operation of data from the annotated element.
         *
         * @param data data
         */
        @Override
        public void execute(Object data) {
            Optional.ofNullable(data)
                .map(t -> delegate.getExtractor().invoke(t))
                .filter(t -> canApply(delegate.getAnnotation().condition(), t))
                .ifPresent(delegate::execute);
        }

        private boolean canApply(String condition, Object data) {
            if (StringUtils.isEmpty(condition)) {
                return true;
            }
            ExpressionContext expressionContext = contextFactory.apply(data);
            return Boolean.TRUE.equals(
                expressionEvaluator.execute(condition, Boolean.class, expressionContext)
            );
        }
    }
}
