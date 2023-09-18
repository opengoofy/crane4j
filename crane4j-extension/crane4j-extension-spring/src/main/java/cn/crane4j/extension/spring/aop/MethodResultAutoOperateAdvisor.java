package cn.crane4j.extension.spring.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.aop.MethodResultAutoOperateSupport;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Automatic filling of aspect with method return value based on Spring AOP implementation
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see MethodResultAutoOperateSupport
 */
@Slf4j
@Getter
public class MethodResultAutoOperateAdvisor
    extends MethodResultAutoOperateSupport implements PointcutAdvisor, MethodInterceptor, DisposableBean {

    private final Pointcut pointcut = AutoOperatePointcut.forAnnotatedMethod(
        (m, c) -> !Objects.equals(m.getReturnType(), Void.TYPE)
            && AnnotatedElementUtils.isAnnotated(m, AutoOperate.class)
    );

    /**
     * Create a {@link MethodResultAutoOperateSupport} instance
     *
     * @param elementResolver           element handler
     * @param expressionExecuteDelegate method base expression evaluator delegate
     */
    public MethodResultAutoOperateAdvisor(
        AutoOperateAnnotatedElementResolver elementResolver,
        MethodBaseExpressionExecuteDelegate expressionExecuteDelegate) {
        super(elementResolver, expressionExecuteDelegate);
        log.info("enable automatic filling of method result");
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        AutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, AutoOperate.class);
        Object result = methodInvocation.proceed();
        try {
            afterMethodInvoke(annotation, method, result, methodInvocation.getArguments());
        } catch (Exception ex) {
            log.error("cannot auto operate result for method [{}]", method);
            ex.printStackTrace();
            throw ex;
        }
        return result;
    }

    @NonNull
    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        methodCaches.clear();
    }
}
