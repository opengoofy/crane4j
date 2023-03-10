package cn.crane4j.springboot.support.expression;

import cn.crane4j.core.support.expression.ExpressionContext;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Expression context implementation based on {@link StandardEvaluationContext}.
 *
 * @author huangchengxing
 * @see SpelExpressionEvaluator
 */
public class SpelExpressionContext extends StandardEvaluationContext implements ExpressionContext {

    /**
     * Create {@link SpelExpressionContext}
     */
    public SpelExpressionContext() {
    }

    /**
     * Create {@link SpelExpressionContext}
     *
     * @param rootObject root object
     */
    public SpelExpressionContext(Object rootObject) {
        super(rootObject);
    }

    /**
     * Create {@link SpelExpressionContext}
     *
     * @param expressionContext expression context
     */
    public SpelExpressionContext(ExpressionContext expressionContext) {
        this(expressionContext.getRoot());
        expressionContext.getVariables().forEach(this::registerVariable);
    }

    /**
     * Get root object.
     *
     * @return root object
     */
    @Override
    public Object getRoot() {
        return getRootObject().getValue();
    }

    /**
     * Set root object.
     *
     * @param root root object
     */
    @Override
    public void setRoot(Object root) {
        super.setRootObject(root);
    }

    /**
     * Register variables.
     *
     * @param name variable names
     * @param value variables
     */
    @Override
    public void registerVariable(String name, Object value) {
        super.setVariable(name, value);
    }

    /**
     * Get variables.
     *
     * @return variables
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getVariables() {
        return (Map<String, Object>)ReflectUtil.getFieldValue(this, "variables");
    }
}
