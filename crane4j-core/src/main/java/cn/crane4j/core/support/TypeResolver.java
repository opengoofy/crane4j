package cn.crane4j.core.support;

import javax.annotation.Nullable;

/**
 * 推断一个{@link Object}实例的实际类型
 *
 * @author huangchengxing
 * @see SimpleTypeResolver
 */
public interface TypeResolver {

    /**
     * 推断类型
     *
     * @param target 对象
     * @return 推断的类型，若对象为{@code null}，则返回{@code null}
     */
    @Nullable
    Class<?> resolve(Object target);
}
