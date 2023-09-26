package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>An object that holds the {@link AutoOperate} annotation and the annotated element,
 * used complete the operation of data from the annotated element
 * for {@link BeanOperations} by {@link BeanOperationExecutor}.
 *
 * <p>If cannot determine the {@link BeanOperations} for the annotated element,
 * we can make {@link #beanOperations} is null, in this case, the {@link #parser} must not be null.
 * When execute the {@link #execute(Object)} method,
 * the {@link #parser} will be used to get actual {@link BeanOperations} for the invoke data.
 *
 * @author huangchengxing
 * @see AutoOperateAnnotatedElementResolver
 * @see AutoOperate
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoOperateAnnotatedMethod implements AutoOperateAnnotatedElement {

    @Getter
    private final AutoOperate annotation;
    @Getter
    private final AnnotatedElement element;
    private final MethodInvoker extractor;
    private final Predicate<? super KeyTriggerOperation> filter;
    @Nullable
    @Getter
    private final BeanOperations beanOperations;
    private final BeanOperationExecutor executor;
    private final Function<Object, BeanOperations> parser;

    public static AutoOperateAnnotatedMethod forStaticTypeOperation(
        AutoOperate annotation, AnnotatedElement element, MethodInvoker extractor,
        Predicate<? super KeyTriggerOperation> filter, BeanOperations operations, BeanOperationExecutor executor) {
        return new AutoOperateAnnotatedMethod(annotation, element, extractor, filter, operations, executor, null);
    }

    public static AutoOperateAnnotatedMethod forDynamicTypeOperation(
        AutoOperate annotation, AnnotatedElement element, MethodInvoker extractor,
        Predicate<? super KeyTriggerOperation> filter, BeanOperationExecutor executor, Function<Object, BeanOperations> parser) {
        return new AutoOperateAnnotatedMethod(annotation, element, extractor, filter, null, executor, parser);
    }

    /**
     * Execute the operation of data from the annotated element.
     *
     * @param data data
     */
    @Override
    public void execute(Object data) {
        // if the beanOperations is null, then use the parser to parse the annotation type
        Object target = extractor.invoke(data);
        if (Objects.isNull(target)) {
            return;
        }
        BeanOperations bo = Objects.isNull(beanOperations) ? parser.apply(target) : beanOperations;
        if (Objects.nonNull(bo) && !bo.isEmpty()) {
            executor.execute(CollectionUtils.adaptObjectToCollection(target), bo, filter);
        }
    }
}
