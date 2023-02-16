package cn.crane4j.core.support.expression;

/**
 * 表达式执行器
 *
 * @author huangchengxing
 */
@FunctionalInterface
public interface ExpressionEvaluator {

    /**
     * 在指定上文中执行表达式，并返回执行结果
     *
     * @param expression 表达式
     * @param resultType 返回值类型
     * @param context 上下文
     * @param <T> 返回值类型
     * @return 执行结果，若无结果则返回{@code null}
     */
    <T> T execute(String expression, Class<T> resultType, ExpressionContext context);
}
