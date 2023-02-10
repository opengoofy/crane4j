package cn.createsequence.crane4j.springboot.support.aop;

import cn.createsequence.crane4j.core.util.CollectionUtils;
import cn.createsequence.crane4j.springboot.annotation.ArgAutoOperate;
import cn.createsequence.crane4j.springboot.annotation.AutoOperate;
import cn.createsequence.crane4j.springboot.support.MethodAnnotatedElementAutoOperateSupport;
import cn.createsequence.crane4j.springboot.support.MethodBaseExpressionEvaluator;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于SpringAOP实现的方法入参自动填充切面
 *
 * @author huangchengxing
 * @see ArgAutoOperate
 * @see AutoOperate
 */
@Slf4j
@Aspect
public class MethodArgumentAutoOperateAspect extends MethodAnnotatedElementAutoOperateSupport implements DisposableBean {

    private static final ResolvedElement[] EMPTY_ELEMENTS = new ResolvedElement[0];
    private final Map<String, ResolvedElement[]> methodParameterCaches = CollectionUtils.newWeakConcurrentMap();

    public MethodArgumentAutoOperateAspect(
        ApplicationContext applicationContext, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator) {
        super(applicationContext, methodBaseExpressionEvaluator);
        log.info("enable automatic filling of method argument");
    }

    @Before("@annotation(cn.createsequence.crane4j.springboot.annotation.ArgAutoOperate)")
    public void before(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ArgAutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(method, ArgAutoOperate.class);
        if (Objects.isNull(annotation)) {
            return;
        }
        Object[] args = joinPoint.getArgs();
        if (ArrayUtil.isEmpty(args)) {
            return;
        }
        ResolvedElement[] elements = MapUtil.computeIfAbsent(
            methodParameterCaches, method.getName(), name -> resolveParameters(annotation, method)
        );
        if (elements == EMPTY_ELEMENTS) {
            return;
        }
        // 根据配置缓存填充方法参数
        log.debug("process arguments for [{}]", method.getName());
        processArguments(method, args, elements);
    }

    private void processArguments(Method method, Object[] args, ResolvedElement[] resolvedElements) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            ResolvedElement element = resolvedElements[i];
            try {
                element.execute(arg);
            } catch (Exception e) {
                log.warn(
                    "cannot process argument [{}] for [{}]: [{}]",
                    method.getName(), ((Parameter)element.getElement()).getName(),
                    ExceptionUtil.getRootCause(e).getMessage()
                );
            }
        }
    }

    private ResolvedElement[] resolveParameters(ArgAutoOperate argAutoOperate, Method method) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return EMPTY_ELEMENTS;
        }
        Map<String, AutoOperate> annotations = resolvedAnnotations(argAutoOperate, parameters);
        // 解析注解，生成缓存对象
        ResolvedElement[] results = new ResolvedElement[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            AutoOperate annotation = annotations.get(parameter.getName());
            ResolvedElement element = Objects.isNull(annotation) ?
                EmptyElement.INSTANCE : resolveElement(parameter, annotation);
            results[i] = element;
        }
        return results;
    }

    private static Map<String, AutoOperate> resolvedAnnotations(ArgAutoOperate argAutoOperate, Parameter[] parameters) {
        // 类上的注解
        Map<String, AutoOperate> annotations = Stream.of(argAutoOperate.value())
            .collect(Collectors.toMap(AutoOperate::value, Function.identity()));
        // 方法参数上的注解
        for (Parameter parameter : parameters) {
            AutoOperate annotation = AnnotatedElementUtils.findMergedAnnotation(parameter, AutoOperate.class);
            if (Objects.nonNull(annotation)) {
                annotations.put(parameter.getName(), annotation);
            }
        }
        return annotations;
    }

    /**
     * 销毁Bean时释放资源
     */
    @Override
    public void destroy() {
        for (ResolvedElement[] elements : methodParameterCaches.values()) {
            Arrays.fill(elements, null);
        }
        methodParameterCaches.clear();
    }

    /**
     * {@link ResolvedElement}的空实现，用于占位
     */
    protected static class EmptyElement extends ResolvedElement {
        protected static final ResolvedElement INSTANCE = new EmptyElement();
        public EmptyElement() {
            super(null, null, null, null, null);
        }
        @Override
        public void execute(Object result) {
            // do nothing
        }
    }
}
