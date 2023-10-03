package cn.crane4j.core.support.auto;

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
     * Whether the resolver supports the element.
     *
     * @param element element
     * @param annotation annotation
     * @return true if supports, otherwise false
     */
    default boolean support(
        AnnotatedElement element, @Nullable AutoOperate annotation) {
        return true;
    }

    /**
     * Resolve the {@link AutoOperate} annotation on the element
     * and build {@link AutoOperateAnnotatedElement} for it according to its configuration.
     *
     * @param element element
     * @param annotation annotation
     * @return {@link AutoOperateAnnotatedElement}
     */
    @Nullable
    AutoOperateAnnotatedElement resolve(
        AnnotatedElement element, @Nullable AutoOperate annotation);
}
