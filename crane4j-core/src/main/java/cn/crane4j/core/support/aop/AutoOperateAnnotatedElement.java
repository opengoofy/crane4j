package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Resolved annotated element.
 *
 * @author huangchengxing
 * @see AutoOperateAnnotatedElementResolver
 * @see AutoOperate
 */
@Getter
@RequiredArgsConstructor
public class AutoOperateAnnotatedElement {
    private final AutoOperate annotation;
    private final AnnotatedElement element;
    private final MethodInvoker extractor;
    private final Predicate<? super KeyTriggerOperation> filter;
    private final BeanOperations beanOperations;
    private final BeanOperationExecutor executor;

    public void execute(Object data) {
        Object target = extractor.invoke(data);
        if (Objects.nonNull(target)) {
            executor.execute(CollectionUtils.adaptObjectToCollection(target), beanOperations, filter);
        }
    }
}
