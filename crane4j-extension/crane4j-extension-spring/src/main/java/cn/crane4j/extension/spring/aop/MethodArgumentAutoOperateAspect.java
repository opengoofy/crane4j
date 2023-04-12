package cn.crane4j.extension.spring.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElement;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.ParameterNameDiscoverer;
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
@Aspect
public class MethodArgumentAutoOperateAspect extends MethodArgumentAutoOperateSupport implements DisposableBean {

    public MethodArgumentAutoOperateAspect(
        AutoOperateAnnotatedElementResolver elementResolver,
        MethodBaseExpressionExecuteDelegate expressionExecuteDelegate,
        ParameterNameDiscoverer parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        super(elementResolver, expressionExecuteDelegate, parameterNameDiscoverer::getParameterNames, annotationFinder);
        log.info("enable automatic filling of method argument");
    }

    @Before("@annotation(cn.crane4j.annotation.ArgAutoOperate)")
    public void beforeMethodInvoke(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ArgAutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, ArgAutoOperate.class);
        super.beforeMethodInvoke(annotation, method, joinPoint.getArgs());
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
}
