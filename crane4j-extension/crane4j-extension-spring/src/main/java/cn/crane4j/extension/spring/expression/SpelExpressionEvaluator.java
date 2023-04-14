package cn.crane4j.extension.spring.expression;

import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import java.util.Map;

/**
 * {@link ExpressionEvaluator} implementation based on spring SpEL.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class SpelExpressionEvaluator implements ExpressionEvaluator, DisposableBean {

    private final Map<String, Expression> expressionCaches = CollectionUtils.newWeakConcurrentMap();
    private final ExpressionParser expressionParser;

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
    @Override
    public <T> T execute(String expression, Class<T> resultType, ExpressionContext context) {
        EvaluationContext evaluationContext = (context instanceof SpelExpressionContext) ?
            (EvaluationContext)context : new SpelExpressionContext(context);
        return parseExpression(expression).getValue(evaluationContext, resultType);
    }

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        expressionCaches.clear();
    }

    private Expression parseExpression(String expression) {
        return CollectionUtils.computeIfAbsent(expressionCaches, expression, expressionParser::parseExpression);
    }
}
