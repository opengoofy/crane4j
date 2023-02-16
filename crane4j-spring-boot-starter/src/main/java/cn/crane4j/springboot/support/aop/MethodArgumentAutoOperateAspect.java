package cn.crane4j.springboot.support.aop;

import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.springboot.annotation.ArgAutoOperate;
import cn.crane4j.springboot.annotation.AutoOperate;
import cn.crane4j.springboot.support.MethodAnnotatedElementAutoOperateSupport;
import cn.crane4j.springboot.support.MethodBaseExpressionEvaluator;
import cn.crane4j.springboot.util.MethodUtils;
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
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public MethodArgumentAutoOperateAspect(
        ApplicationContext applicationContext, MethodBaseExpressionEvaluator methodBaseExpressionEvaluator,
        ParameterNameDiscoverer parameterNameDiscoverer) {
        super(applicationContext, methodBaseExpressionEvaluator);
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        log.info("enable automatic filling of method argument");
    }

    @Before("@annotation(cn.crane4j.springboot.annotation.ArgAutoOperate)")
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
        Map<String, Parameter> parameterMap = MethodUtils.resolveParameterNames(parameterNameDiscoverer, method);
        Map<String, AutoOperate> methodLevelAnnotations = Stream.of(argAutoOperate.value())
            .collect(Collectors.toMap(AutoOperate::value, Function.identity()));

        ResolvedElement[] results = new ResolvedElement[parameterMap.size()];
        int index = 0;
        for (Map.Entry<String, Parameter> entry : parameterMap.entrySet()) {
            // 先优先从参数上获取，没有再从方法上获取
            String paramName = entry.getKey();
            Parameter param = entry.getValue();
            AutoOperate annotation = Optional
                .ofNullable(AnnotatedElementUtils.findMergedAnnotation(param, AutoOperate.class))
                .orElse(methodLevelAnnotations.get(paramName));
            // 解析注解，生成缓存对象
            ResolvedElement element = Objects.isNull(annotation) ?
                EmptyElement.INSTANCE : resolveElement(param, annotation);
            results[index++] = element;
        }
        return results;
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
