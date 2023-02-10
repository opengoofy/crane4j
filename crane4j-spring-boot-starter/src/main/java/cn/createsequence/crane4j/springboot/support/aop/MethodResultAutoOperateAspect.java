package cn.createsequence.crane4j.springboot.support.aop;

import cn.createsequence.crane4j.core.util.CollectionUtils;
import cn.createsequence.crane4j.springboot.annotation.AutoOperate;
import cn.createsequence.crane4j.springboot.support.MethodAnnotatedElementAutoOperateSupport;
import cn.createsequence.crane4j.springboot.support.MethodBaseExpressionEvaluator;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * 基于SpringAOP实现的方法返回值自动填充切面
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
        ApplicationContext applicationContext, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator) {
        super(applicationContext, methodBaseExpressionEvaluator);
        log.info("enable automatic filling of method result");
    }

    @AfterReturning(returning = "result", pointcut = "@annotation(cn.createsequence.crane4j.springboot.annotation.AutoOperate)")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        if (Objects.isNull(result)) {
            return;
        }
        // 获取注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, AutoOperate.class);
        if (Objects.isNull(annotation)) {
            return;
        }
        // 是否应用该操作
        String condition = annotation.condition();
        if (!checkSupport(joinPoint.getArgs(), result, method, condition)) {
            return;
        }
        // 获取/构建方法缓存并执行操作
        log.debug("process result for [{}]", method.getName());
        ResolvedElement element = MapUtil.computeIfAbsent(methodCaches, method.getName(), m -> resolveElement(method, annotation));
        try {
            element.execute(result);
        } catch (Exception e) {
            log.warn("cannot process result for [{}]: [{}]", method.getName(), ExceptionUtil.getRootCause(e).getMessage());
        }
    }

    /**
     * 销毁Bean时释放资源
     */
    @Override
    public void destroy() {
        methodCaches.clear();
    }
}
