package cn.createsequence.crane4j.core.support;

import cn.createsequence.crane4j.core.util.CollectionUtils;

import java.util.Objects;

/**
 * <p>{@link TypeResolver}的基本实现。<br />
 * 将输入对象适配为集合，然后获取集合中第一个非{@code null}元素，并返回该元素的类型，
 * 若输入对象为{@code null}，或者集合中的所有元素都为{@code null}，则返回{@code null}。
 *
 * @author huangchengxing
 */
public class SimpleTypeResolver implements TypeResolver {

    /**
     * 推断类型
     *
     * @param target 对象
     * @return 推断的类型
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
