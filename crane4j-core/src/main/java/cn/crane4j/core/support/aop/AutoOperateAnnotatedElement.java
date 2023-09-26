package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;

/**
 * <p>An object that holds the {@link AutoOperate} annotation and the annotated element,
 * used complete the operation of data from the annotated element
 * for {@link BeanOperations} by {@link BeanOperationExecutor}.
 *
 * @author huangchengxing
 * @see AutoOperateAnnotatedElementResolver
 * @see AutoOperate
 */
public interface AutoOperateAnnotatedElement {

    /**
     * get the {@link AutoOperate} annotation.
     *
     * @return annotation
     */
    AutoOperate getAnnotation();

    /**
     * Get the annotated element.
     *
     * @return element
     */
    AnnotatedElement getElement();

    /**
     * <p>Get the {@link BeanOperations} for the annotated element.<br/>
     * If the resolver cannot determine the {@link BeanOperations} for the annotated element exactly,
     * it can return null, in this case, the {@link BeanOperations} will be resolved in the runtime.
     *
     * @return beanOperations
     */
    @Nullable
    BeanOperations getBeanOperations();

    /**
     * Execute the operation of data from the annotated element.
     *
     * @param data data
     */
    void execute(Object data);
}
