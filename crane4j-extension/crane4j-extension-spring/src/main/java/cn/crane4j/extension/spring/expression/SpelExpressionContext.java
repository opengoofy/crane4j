package cn.crane4j.extension.spring.expression;

import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.util.ReflectUtils;
import lombok.NoArgsConstructor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Expression context implementation based on {@link StandardEvaluationContext}.
 *
 * @author huangchengxing
 * @see SpelExpressionEvaluator
 */
@NoArgsConstructor
public class SpelExpressionContext extends StandardEvaluationContext implements ExpressionContext {

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
    @Override
    public Map<String, Object> getVariables() {
        return ReflectUtils.getFieldValue(this, "variables");
    }
}
