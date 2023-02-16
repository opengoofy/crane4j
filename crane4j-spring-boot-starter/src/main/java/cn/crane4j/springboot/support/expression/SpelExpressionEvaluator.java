package cn.crane4j.springboot.support.expression;

import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import java.util.Map;

/**
 * 基于SpEL表达式的执行器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class SpelExpressionEvaluator implements ExpressionEvaluator, DisposableBean {

    private final Map<String, Expression> expressionCaches = CollectionUtils.newWeakConcurrentMap();
    private final ExpressionParser expressionParser;

    /**
     * 在指定上文中执行表达式，并返回执行结果
     *
     * @param expression 表达式
     * @param resultType 返回值类型
     * @param context 上下文
     * @param <T> 返回值类型
     * @return 执行结果，若无结果则返回{@code null}
     */
    @Override
    public <T> T execute(String expression, Class<T> resultType, ExpressionContext context) {
        EvaluationContext evaluationContext = (context instanceof SpelExpressionContext) ?
            (EvaluationContext)context : new SpelExpressionContext(context);
        return parseExpression(expression).getValue(evaluationContext, resultType);
    }

    /**
     * 销毁Bean时释放资源
     */
    @Override
    public void destroy() {
        expressionCaches.clear();
    }

    private Expression parseExpression(String expression) {
        return MapUtil.computeIfAbsent(expressionCaches, expression, expressionParser::parseExpression);
    }
}
