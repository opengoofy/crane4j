package cn.createsequence.crane4j.core.support.expression;

import java.util.Map;

/**
 * 表达式上下文抽象，该接口用于保证无论表达式功能基于哪些引擎实现，
 * 表达式都能在保证基本的变量和根对象设置的功能上正常执行。
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
