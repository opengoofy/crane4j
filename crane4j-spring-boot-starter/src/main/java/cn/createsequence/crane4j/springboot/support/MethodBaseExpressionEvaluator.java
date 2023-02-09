package cn.createsequence.crane4j.springboot.support;

import cn.createsequence.crane4j.core.support.expression.ExpressionContext;
import cn.createsequence.crane4j.core.support.expression.ExpressionEvaluator;
import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 用于执行以方法调用为上下文的表达式的计算器
 *
 * @author huangchengxing
 * @see MethodAnnotatedElementAutoOperateSupport
 */
@RequiredArgsConstructor
public class MethodBaseExpressionEvaluator {

    public static final String RESULT = "result";
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final ExpressionEvaluator expressionEvaluator;
    private final Function<Method, ExpressionContext> contextFactory;

    /**
     * 在指定上文中执行表达式，并返回执行结果
     *
     * @param expression 表达式
     * @param resultType 返回值类型
     * @param execution    上下文
     * @return 执行结果，若无结果则返回{@code null}
     */
    public <T> T execute(String expression, Class<T> resultType, MethodExecution execution) {
        ExpressionContext context = resolveContext(execution);
        return expressionEvaluator.execute(expression, resultType, context);
    }

    /**
     * 创建一个方法切面表达式上下文
     *
     * @param methodExecution the function argument
     * @return the function result
     */
    protected ExpressionContext resolveContext(MethodExecution methodExecution) {
        ExpressionContext context = contextFactory.apply(methodExecution.getMethod());
        // 解析并注册方法参数
        registerParams(methodExecution, context);
        // 注册返回值
        context.registerVariable(RESULT, methodExecution.getResult());
        return context;
    }

    /**
     * 向上下文中注册方法的执行参数
     *
     * @param methodExecution 方法执行参数
     * @param context 执行上下文
     */
    protected void registerParams(MethodExecution methodExecution, ExpressionContext context) {
        String[] paramNames = parameterNameDiscoverer.getParameterNames(methodExecution.getMethod());
        Object[] args = methodExecution.getArgs();
        Map<String, Object> paramsMap = resolvedParams(paramNames, args);
        paramsMap.forEach(context::registerVariable);
    }

    /**
     * 解析方法入参
     *
     * @param paramNames 参数名称
     * @param args 入参
     * @return 参数名称与入参的对应集合
     */
    protected Map<String, Object> resolvedParams(String[] paramNames, Object[] args) {
        if (ArrayUtil.isEmpty(paramNames)) {
            return Collections.emptyMap();
        }
        Map<String, Object> results = new LinkedHashMap<>(paramNames.length);
        int argCount = args.length;
        for (int i = 0; i < paramNames.length; i++) {
            String name = paramNames[i];
            Object arg = i < argCount ? args[i] : null;
            results.put(name, arg);
        }

        return results;
    }

    /**
     * 表达式切面上下文，用于根据需求构建用于执行表达式的{@link ExpressionContext}
     */
    @Getter
    @RequiredArgsConstructor
    public static class MethodExecution {
        private final Object[] args;
        private final Method method;
        private final Object result;
        private final String condition;
    }
}
