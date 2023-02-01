package cn.createsequence.crane4j.core.support.reflect;

import cn.createsequence.crane4j.core.support.MethodInvoker;

import javax.annotation.Nullable;

/**
 * 对象属性值操作者，用于读写对象属性
 *
 * @author huangchengxing
 * @see AsmReflectPropertyOperator
 * @see ReflectPropertyOperator
 */
public interface PropertyOperator {

    /**
     * 获取指定属性
     *
     * @param target 对象
     * @param targetType 目标类型
     * @param propertyName 属性名称
     * @return 属性值
     */
    @Nullable
    Object readProperty(Class<?> targetType, Object target, String propertyName);

    /**
     * 获取Getter方法
     *
     * @param targetType 目标类型
     * @param propertyName 方法名称
     * @return 找到的方法，若没找到则返回{@code null}
     */
    @Nullable
    MethodInvoker findGetter(Class<?> targetType, String propertyName);

    /**
     * 将值写入指定属性
     *
     * @param target 对象
     * @param targetType 目标类型
     * @param propertyName 属性名称
     * @param value 属性值
     */
    void writeProperty(Class<?> targetType, Object target, String propertyName, Object value);

    /**
     * 获取Setter方法
     *
     * @param targetType 目标类型
     * @param propertyName 方法名称
     * @return 找到的方法，若没找到则返回{@code null}
     */
    @Nullable
    MethodInvoker findSetter(Class<?> targetType, String propertyName);
}
