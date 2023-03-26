package cn.crane4j.extension.expression;

import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.extension.support.ParameterNameFinder;
import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Method base expression evaluator delegate.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class MethodBaseExpressionEvaluatorDelegate {

    public static final String RESULT = "result";
    private final ParameterNameFinder parameterNameDiscoverer;
    protected final ExpressionEvaluator expressionEvaluator;
    private final Function<Method, ExpressionContext> contextFactory;

    /**
     * Execute the expression in the specified above and return the execution result.
     *
     * @param expression expression
     * @param resultType result type
     * @param execution execution
     * @return execution result
     */
    @Nullable
    public <T> T execute(String expression, Class<T> resultType, MethodExecution execution) {
        ExpressionContext context = resolveContext(execution);
        return expressionEvaluator.execute(expression, resultType, context);
    }

    /**
     * Create a method aspect expression context.
     *
     * @param methodExecution the function argument
     * @return the function result
     */
    protected ExpressionContext resolveContext(MethodExecution methodExecution) {
        ExpressionContext context = contextFactory.apply(methodExecution.getMethod());
        // resolve and register arguments
        registerParams(methodExecution, context);
        // register return value
        context.registerVariable(RESULT, methodExecution.getResult());
        return context;
    }

    /**
     * Register the execution parameters of the method in the context.
     *
     * @param methodExecution method execution
     * @param context context
     */
    protected void registerParams(MethodExecution methodExecution, ExpressionContext context) {
        String[] paramNames = parameterNameDiscoverer.getParameterNames(methodExecution.getMethod());
        Object[] args = methodExecution.getArgs();
        Map<String, Object> paramsMap = resolvedParams(paramNames, args);
        paramsMap.forEach(context::registerVariable);
    }

    /**
     * Resolve method input parameter.
     *
     * @param paramNames param names
     * @param args args
     * @return A collection of parameter names and input parameters
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
     * Expression aspect context, which is used to build {@link ExpressionContext} for expression execution.
     */
    @Getter
    @RequiredArgsConstructor
    public static class MethodExecution {
        private final Object[] args;
        private final Method method;
        private final Object result;
    }
}
