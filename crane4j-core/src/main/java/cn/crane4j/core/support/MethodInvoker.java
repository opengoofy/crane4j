package cn.crane4j.core.support;

/**
 * Represents a method that can be called.
 *
 * @author huangchengxing
 */
@FunctionalInterface
public interface MethodInvoker {

    /**
     * Invoke method.
     *
     * @param target target
     * @param args args
     * @return result of invoke
     */
    Object invoke(Object target, Object... args);
}
