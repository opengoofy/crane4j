package cn.createsequence.crane4j.core.support;

/**
 * @author huangchengxing
 */
@FunctionalInterface
public interface MethodInvoker {

    /**
     * 调用方法
     *
     * @param target 对象
     * @param args 参数
     * @return 调用结果
     */
    Object invoke(Object target, Object... args);
}
