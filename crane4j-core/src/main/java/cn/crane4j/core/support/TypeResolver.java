package cn.crane4j.core.support;

import javax.annotation.Nullable;

/**
 * A resolver for Infer the actual type of {@link Object} instance.
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
