package cn.crane4j.core.support.expression;

import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.util.ArrayUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>A delegate class for executing expressions in the method aspect,
 * generally supporting the awareness of method parameters and return values in expressions.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class MethodBaseExpressionExecuteDelegate {

    public static final String RESULT = "result";
    protected final ParameterNameFinder parameterNameDiscoverer;
    protected final ExpressionEvaluator expressionEvaluator;
    protected final Function<Method, ExpressionContext> contextFactory;

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
     * Execute the expression in the specified above and return the execution result.
     *
     * @param expression expression
     * @param resultType result type
     * @param method method
     * @param args args
     * @param result result
     * @return execution result
     */
    @Nullable
    public <T> T execute(String expression, Class<T> resultType, Method method, Object[] args, Object result) {
        return execute(expression, resultType, new MethodBaseExpressionExecuteDelegate.MethodExecution(args, method, result));
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
        for (int i = 0; i < args.length; i++) {
            context.registerVariable("a" + i, args[i]);
        }
    }

    /**
     * Resolve method input parameter.
     *
     * @param paramNames param names
     * @param args args
     * @return A collection of parameter names and input parameters
     */
    protected Map<String, Object> resolvedParams(String[] paramNames, Object[] args) {
        if (ArrayUtils.isEmpty(paramNames)) {
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
