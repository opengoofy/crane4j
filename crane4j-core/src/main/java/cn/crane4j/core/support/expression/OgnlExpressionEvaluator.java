package cn.crane4j.core.support.expression;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.map.MapUtil;
import lombok.SneakyThrows;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * {@link ExpressionEvaluator} implementation based on Ognl.
 *
 * @author huangchengxing
 */
public class OgnlExpressionEvaluator implements ExpressionEvaluator {

    private final Map<String, Object> expressionCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * Execute the expression in the specified above and return the execution result.
     *
     * @param expression expression
     * @param resultType result type
     * @param context    context
     * @return result of execution
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T execute(String expression, Class<T> resultType, ExpressionContext context) {
        Object exp = MapUtil.computeIfAbsent(expressionCaches, expression, ex -> {
            Object result;
            try {
                result = Ognl.parseExpression(ex);
            } catch (OgnlException e) {
                throw new Crane4jException(e);
            }
            return result;
        });
        context = context instanceof OgnlContext ? context : new OgnlExpressionContext(context);
        return (T)Ognl.getValue(exp, context, resultType);
    }
}
