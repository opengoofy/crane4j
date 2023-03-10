package cn.crane4j.core.support.expression;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Expression executor.
 *
 * @author huangchengxing
 */
@FunctionalInterface
public interface ExpressionEvaluator {

    /**
     * Execute the expression in the specified above and return the execution result.
     *
     * @param expression expression
     * @param resultType result type
     * @param context context
     * @param <T> return type
     * @return result of execution
     */
    @Nullable
    <T> T execute(String expression, Class<T> resultType, ExpressionContext context);
}
