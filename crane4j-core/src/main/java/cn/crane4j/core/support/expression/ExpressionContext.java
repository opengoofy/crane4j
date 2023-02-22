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
     * 获取根对象
     *
     * @return 根对象
     */
    Object getRoot();

    /**
     * 设置根对象
     *
     * @param root 根对象
     */
    void setRoot(Object root);

    /**
     * 注册变量
     *
     * @param name 变量名
     * @param value 变量值
     */
    void registerVariable(String name, Object value);

    /**
     * 获取变量
     *
     * @return 变量
     */
    Map<String, Object> getVariables();
}
