package cn.createsequence.crane4j.core.support.reflect;

import cn.createsequence.crane4j.core.support.MethodInvoker;
import cn.hutool.core.util.ReflectUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * 基于原生反射实现的反射工具类
 *
 * @author huangchengxing
 */
public class ReflectPropertyOperator extends CacheablePropertyOperator {

    /**
     * 根据指定方法创建调用器
     *
     * @param targetType   目标类型
     * @param propertyName 属性名称
     * @param method       属性的getter或setter方法
     * @return 调用器
     */
    @Override
    protected MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method) {
        return new ReflectMethodInvoker(method);
    }

    /**
     * 基于原生反射实现的方法调用
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public static class ReflectMethodInvoker implements MethodInvoker {

        /**
         * 要调用的方法
         */
        @NonNull
        private final Method method;

        /**
         * 调用方法
         *
         * @param target 对象
         * @param args 参数
         * @return 调用结果
         */
        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return ReflectUtil.invoke(target, method, args);
        }
    }
}
