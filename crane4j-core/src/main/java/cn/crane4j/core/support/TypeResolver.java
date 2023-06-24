package cn.crane4j.core.support;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A handler that infers the type of object.
 *
 * @author huangchengxing
 * @see SimpleTypeResolver
 */
public interface TypeResolver {

    /**
     * Inference type.
     *
     * @param target target
     * @return Inferred type. If the object is {@code null}, return {@code null}
     */
    @Nullable
    Class<?> resolve(Object target);
}
