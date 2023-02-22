package cn.crane4j.core.support;

import cn.crane4j.core.util.CollectionUtils;

import java.util.Objects;

/**
 * <p>The basic implementation of {@link TypeResolver}.<br />
 * Adapt the input object to a collection, then get
 * the first non-null element in the collection, and return the type of the element.<br />
 * If the input object is {@code null},
 * or all elements in the collection are {@code null}, {@code null} is returned.
 *
 * @author huangchengxing
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
        return CollectionUtils.adaptObjectToCollection(target).stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(Object::getClass)
            .orElse(null);
    }
}
