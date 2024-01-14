package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;

/**
 * <p>An operator proxy method factory that supports auto operate for method parameters when calling method.<br/>
 * compare to {@link ParametersFillProxyMethodFactory}, this class follows the rule of {@link AutoOperate},
 * it should always perform before {@link ParametersFillProxyMethodFactory}.
 *
 * @author huangchengxing
 * @see MethodArgumentAutoOperateSupport
 * @see ParametersFillProxyMethodFactory
 * @see ArgAutoOperate
 * @see AutoOperate
 * @since 2.4.0
 */
@Slf4j
public class ArgAutoOperateProxyMethodFactory
    extends MethodArgumentAutoOperateSupport implements OperatorProxyMethodFactory {

    /**
     * Create a {@link MethodArgumentAutoOperateSupport} instance.
     *
     * @param elementResolver           element handler
     * @param expressionExecuteDelegate expression evaluator delegate
     * @param parameterNameFinder       parameter name finder
     * @param annotationFinder          annotation finder
     */
    public ArgAutoOperateProxyMethodFactory(
        AutoOperateAnnotatedElementResolver elementResolver, MethodBaseExpressionExecuteDelegate expressionExecuteDelegate,
        ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        super(elementResolver, expressionExecuteDelegate, parameterNameFinder, annotationFinder);
    }

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return OperatorProxyMethodFactory.ARG_AUTO_OPERATE_PROXY_METHOD_FACTORY_ORDER;
    }

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method                method with at least one parameter
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable
    @Override
    public MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        if (method.getParameterCount() == 0) {
            return null;
        }
        if (!annotationFinder.isAnnotated(method, ArgAutoOperate.class)) {
            return null;
        }
        log.info("create auto operate proxy method for method: {}", method);
        return new AutoOperateOperatorMethodInvoker(method);
    }

    @RequiredArgsConstructor
    private class AutoOperateOperatorMethodInvoker implements MethodInvoker {
        private final Method method;
        @Override
        public Object invoke(Object target, Object... args) {
            beforeMethodInvoke(method, args);
            return null;
        }
    }
}
