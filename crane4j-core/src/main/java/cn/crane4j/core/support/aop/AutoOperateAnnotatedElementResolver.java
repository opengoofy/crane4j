package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.AutoOperate;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;

/**
 * <p>An handler that resolve the {@link AutoOperate}
 * annotation on the element to {@link AutoOperateAnnotatedElement}.
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see AutoOperateAnnotatedElement
 * @see MethodBasedAutoOperateAnnotatedElementResolver
 */
public interface AutoOperateAnnotatedElementResolver {

    /**
     * Resolve the {@link AutoOperate} annotation on the element
     * and build {@link AutoOperateAnnotatedElement} for it according to its configuration.
     *
     * @param element element
     * @param annotation annotation
     * @return {@link AutoOperateAnnotatedElement}
     */
    AutoOperateAnnotatedElement resolve(
        AnnotatedElement element, @Nullable AutoOperate annotation);
}
