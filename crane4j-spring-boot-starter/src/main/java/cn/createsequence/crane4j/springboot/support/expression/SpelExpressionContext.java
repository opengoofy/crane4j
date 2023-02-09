package cn.createsequence.crane4j.springboot.support.expression;

import cn.createsequence.crane4j.core.support.expression.ExpressionContext;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * 基于{@link StandardEvaluationContext}的表达式上下文实现
 *
 * @author huangchengxing
 * @see SpelExpressionEvaluator
 */
public class SpelExpressionContext extends StandardEvaluationContext implements ExpressionContext {

    /**
     * 创建一个表达式上下文对象
     */
    public SpelExpressionContext() {
    }

    /**
     * 创建表达式上下文
     *
     * @param rootObject 根对象
     */
    public SpelExpressionContext(Object rootObject) {
        super(rootObject);
    }

    /**
     * 创建一个表达式上下文对象
     *
     * @param expressionContext 表达式上下文
     */
    public SpelExpressionContext(ExpressionContext expressionContext) {
        this(expressionContext.getRoot());
        expressionContext.getVariables().forEach(this::registerVariables);
    }

    /**
     * 获取根对象
     *
     * @return 根对象
     */
    @Override
    public Object getRoot() {
        return getRootObject().getValue();
    }

    /**
     * 设置根对象
     *
     * @param root 根对象
     */
    @Override
    public void setRoot(Object root) {
        super.setRootObject(root);
    }

    /**
     * 注册变量
     *
     * @param name 变量名
     * @param value 变量值
     */
    @Override
    public void registerVariables(String name, Object value) {
        super.setVariable(name, value);
    }

    /**
     * 获取变量
     *
     * @return 变量
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getVariables() {
        return (Map<String, Object>)ReflectUtil.getFieldValue(this, "variables");
    }
}
