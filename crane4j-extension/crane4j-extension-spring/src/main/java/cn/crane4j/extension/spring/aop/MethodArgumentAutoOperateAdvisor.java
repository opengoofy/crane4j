package cn.crane4j.extension.spring.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElement;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
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
import java.util.Arrays;

/**
 * Method input parameter automatic filling Aspect based on Spring AOP implementation.
 *
 * @author huangchengxing
 * @see ArgAutoOperate
 * @see AutoOperate
 * @see MethodArgumentAutoOperateSupport
 */
@Slf4j
@Getter
public class MethodArgumentAutoOperateAdvisor
    extends MethodArgumentAutoOperateSupport implements PointcutAdvisor, MethodInterceptor, DisposableBean {

    private final Pointcut pointcut = AutoOperatePointcut.forAnnotatedMethod(
        (m, c) -> m.getParameterCount() > 0
            && (AnnotatedElementUtils.isAnnotated(m, ArgAutoOperate.class)
            || Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(AutoOperate.class)))
    );

    public MethodArgumentAutoOperateAdvisor(
        AutoOperateAnnotatedElementResolver elementResolver,
        MethodBaseExpressionExecuteDelegate expressionExecuteDelegate,
        ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        super(elementResolver, expressionExecuteDelegate, parameterNameFinder, annotationFinder);
        log.info("enable automatic filling of method argument");
    }

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        for (AutoOperateAnnotatedElement[] elements : methodParameterCaches.values()) {
            Arrays.fill(elements, null);
        }
        methodParameterCaches.clear();
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        try {
            beforeMethodInvoke(method, methodInvocation.getArguments());
        } catch (Exception ex) {
            log.error("cannot auto operate input arguments for method [{}]", method);
            ex.printStackTrace();
            throw ex;
        }
        return methodInvocation.proceed();
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
}
