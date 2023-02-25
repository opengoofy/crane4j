package cn.crane4j.core.support.expression;

import java.util.Map;

/**
 * <p>A top expression context abstract.<br />
 * This interface is used to ensure that the expression can be executed normally on
 * the basis of the basic variable and root object settings,
 * regardless of the engine implementation of the expression function.
 *
 * @author huangchengxing
 */
public interface ExpressionContext {

    /**
     * Get root object.
     *
     * @return root object
     */
    Object getRoot();

    /**
     * Set root object.
     *
     * @param root root object
     */
    void setRoot(Object root);

    /**
     * Register variables.
     *
     * @param name variable names
     * @param value variables
     */
    void registerVariable(String name, Object value);

    /**
     * Get variables.
     *
     * @return variables
     */
    Map<String, Object> getVariables();
}
