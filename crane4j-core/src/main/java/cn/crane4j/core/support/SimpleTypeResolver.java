package cn.crane4j.core.support;

import cn.crane4j.core.util.ObjectUtils;

/**
 * <p>The basic implementation of {@link TypeResolver}.
 *
 * @author huangchengxing
 * @see ObjectUtils#getElementType
 */
public class SimpleTypeResolver implements TypeResolver {

    /**
     * Inference type.
     *
     * @param target target
     * @return Inferred type. If the object is {@code null}, return {@code null}
     */
    @Override
    public Class<?> resolve(Object target) {
        return ObjectUtils.getElementType(target);
    }
}
