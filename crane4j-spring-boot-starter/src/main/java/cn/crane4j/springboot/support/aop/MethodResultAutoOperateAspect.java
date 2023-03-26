package cn.crane4j.springboot.support.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.springboot.support.MethodAnnotatedElementAutoOperateSupport;
import cn.crane4j.springboot.support.ResolvableExpressionEvaluator;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * Automatic filling of aspect with method return value based on Spring AOP implementation
 *
 * @author huangchengxing
 * @see AutoOperate
 */
@Slf4j
@Aspect
public class MethodResultAutoOperateAspect
    extends MethodAnnotatedElementAutoOperateSupport implements DisposableBean {

    private final Map<String, ResolvedElement> methodCaches = CollectionUtils.newWeakConcurrentMap();

    public MethodResultAutoOperateAspect(
        Crane4jGlobalConfiguration configuration, ResolvableExpressionEvaluator resolvableExpressionEvaluator) {
        super(configuration, resolvableExpressionEvaluator);
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
        if (Objects.isNull(annotation)) {
            return;
        }
        // whether to apply the operation?
        String condition = annotation.condition();
        if (!checkSupport(joinPoint.getArgs(), result, method, condition)) {
            return;
        }
        // get and build method cache
        log.debug("process result for [{}]", method.getName());
        ResolvedElement element = MapUtil.computeIfAbsent(methodCaches, method.getName(), m -> resolveElement(method, annotation));
        try {
            element.execute(result);
        } catch (Exception e) {
            log.warn("cannot process result for [{}]: [{}]", method.getName(), ExceptionUtil.getRootCause(e).getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clear resources when destroying the bean.
     */
    @Override
    public void destroy() {
        methodCaches.clear();
    }
}
