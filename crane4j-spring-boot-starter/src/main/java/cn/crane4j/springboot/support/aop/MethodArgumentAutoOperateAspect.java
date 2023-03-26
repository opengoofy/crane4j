package cn.crane4j.springboot.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport;
import cn.crane4j.core.support.aop.MethodBaseExpressionExecuteDelegate;
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
 */
@Slf4j
@Aspect
public class MethodArgumentAutoOperateAspect extends MethodArgumentAutoOperateSupport implements DisposableBean {

    public MethodArgumentAutoOperateAspect(
        Crane4jGlobalConfiguration configuration,
        MethodBaseExpressionExecuteDelegate methodBaseExpressionExecuteDelegate,
        ParameterNameDiscoverer parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        super(configuration, methodBaseExpressionExecuteDelegate, parameterNameDiscoverer::getParameterNames, annotationFinder);
        log.info("enable automatic filling of method argument");
    }

    @Before("@annotation(cn.crane4j.annotation.ArgAutoOperate)")
    public void before(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ArgAutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, ArgAutoOperate.class);
        beforeMethodInvoke(annotation, method, joinPoint.getArgs());
    }

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        for (ResolvedElement[] elements : methodParameterCaches.values()) {
            Arrays.fill(elements, null);
        }
        methodParameterCaches.clear();
    }
}
