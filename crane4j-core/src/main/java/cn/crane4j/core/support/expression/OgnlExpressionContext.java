package cn.crane4j.core.support.expression;

import ognl.OgnlContext;

import java.util.Map;

/**
 * Expression context implementation based on {@link OgnlContext}.
 *
 * @author huangchengxing
 * @see OgnlContext
 */
public class OgnlExpressionContext extends OgnlContext implements ExpressionContext {

    /**
     * Create {@link OgnlExpressionContext}
     */
    public OgnlExpressionContext() {
        this((Object)null);
    }

    /**
     * Create {@link OgnlExpressionContext}
     *
     * @param root root
     */
    public OgnlExpressionContext(Object root) {
        setRoot(root);
    }

    /**
     * Create {@link OgnlExpressionContext}
     *
     * @param context expression context
     */
    public OgnlExpressionContext(ExpressionContext context) {
        this(context.getRoot());
        putAll(context.getVariables());
    }

    /**
     * Register variables.
     *
     * @param name  variable names
     * @param value variables
     */
    @Override
    public void registerVariable(String name, Object value) {
        super.put(name, value);
    }

    /**
     * Get variables.
     *
     * @return variables
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getVariables() {
        return super.getValues();
    }
}
