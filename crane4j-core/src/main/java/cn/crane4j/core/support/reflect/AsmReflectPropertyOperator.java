package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.hutool.core.map.MapUtil;
import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于ASM实现的反射工具类
 *
 * @author huangchengxing
 */
public class AsmReflectPropertyOperator extends CacheablePropertyOperator {

    /**
     * 方法访问器缓存
     */
    private final Map<Class<?>, MethodAccess> methodAccessCaches = new ConcurrentHashMap<>();

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
        MethodAccess access = MapUtil.computeIfAbsent(methodAccessCaches, targetType, MethodAccess::get);
        int methodIndex = access.getIndex(method.getName(), method.getParameterTypes());
        return new ReflectAsmMethodInvoker(methodIndex, access);
    }

    /**
     * 基于{@link MethodAccess}实现的方法调用
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public static class ReflectAsmMethodInvoker implements MethodInvoker {

        private final int methodIndex;
        private final MethodAccess methodAccess;

        /**
         * 调用方法
         *
         * @param target 对象
         * @param args 参数
         * @return 调用结果
         */
        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return methodAccess.invoke(target, methodIndex, args);
        }
    }
}
