package cn.crane4j.extension.spring.aop;

import cn.crane4j.core.util.ReflectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public class AutoOperatePointcut implements Pointcut {

    private final ClassFilter classFilter;
    private final MethodMatcher methodMatcher;

    public static AutoOperatePointcut create(ClassFilter filter, BiPredicate<Method, Class<?>> predicate) {
        MethodMatcher matcher = (AutoOperateMethodMatcher) predicate::test;
        return new AutoOperatePointcut(filter, matcher);
    }

    public static AutoOperatePointcut forAnnotatedMethod(BiPredicate<Method, Class<?>> predicate) {
        // unable to determine the package in which it is located, maybe is a proxy class？
        ClassFilter filter = t -> Objects.isNull(t.getPackage())
            || (!ReflectUtils.isJdkElement(t) && !t.getPackage().getName().startsWith("org.springframework."));
        return create(filter, predicate);
    }

    @FunctionalInterface
    interface AutoOperateMethodMatcher extends MethodMatcher {

        /**
         * Always return {@code false}.
         *
         * @return false
         */
        @Override
        default boolean isRuntime() {
            return false;
        }

        /**
         * Always return {@code false}.
         *
         * @param method method
         * @param aClass aClass
         * @param objects objects
         * @return false
         */
        @Override
        default boolean matches(@NonNull Method method, @NonNull Class<?> aClass, @NonNull Object... objects) {
            return false;
        }
    }
}
