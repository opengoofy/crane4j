package cn.crane4j.springboot.support.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.extension.aop.MethodResultAutoOperateSupport;
import cn.crane4j.extension.expression.MethodBaseExpressionEvaluatorDelegate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Automatic filling of aspect with method return value based on Spring AOP implementation
 *
 * @author huangchengxing
 * @see AutoOperate
 */
@Slf4j
@Aspect
public class MethodResultAutoOperateAspect extends MethodResultAutoOperateSupport implements DisposableBean {

    public MethodResultAutoOperateAspect(
        Crane4jGlobalConfiguration configuration, MethodBaseExpressionEvaluatorDelegate methodBaseExpressionEvaluatorDelegate) {
        super(configuration, methodBaseExpressionEvaluatorDelegate);
        log.info("enable automatic filling of method result");
    }

    @AfterReturning(returning = "result", pointcut = "@annotation(cn.crane4j.annotation.AutoOperate)")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        if (Objects.isNull(result)) {
            return;
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, AutoOperate.class);
        afterMethodInvoker(annotation, method, result, joinPoint.getArgs());
    }

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        methodCaches.clear();
    }
}
