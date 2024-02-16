package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElement;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.expression.MethodBaseExpressionExecuteDelegate;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Support class for completing the operation of data
 * from the method parameters which annotated by {@link AutoOperate}
 * before the method is called.
 *
 * <p>Before the method is called, the method parameters will be resolved
 * to {@link AutoOperateAnnotatedElement} array by {@link AutoOperateAnnotatedElement},
 * and then the {@link AutoOperateAnnotatedElement} array will be cached.<br />
 * When the method is called, the {@link AutoOperateAnnotatedElement} array
 * will be used to complete the operation of data from the method parameters.
 *
 * <p>Support expression for {@link AutoOperate#condition()}, if the expression is not empty,
 * the expression will be evaluated by {@link MethodBaseExpressionExecuteDelegate},
 * only when the expression returns true or "true", the operation will be applied.
 *
 * @author huangchengxing
 * @see AutoOperateAnnotatedElementResolver
 * @see MethodBaseExpressionExecuteDelegate
 * @see AutoOperate
 * @see ArgAutoOperate
 */
@Slf4j
public class MethodArgumentAutoOperateSupport {

    protected static final AutoOperateAnnotatedElement[] EMPTY_ELEMENTS = new AutoOperateAnnotatedElement[0];
    protected final AutoOperateAnnotatedElementResolver elementResolver;
    protected final Map<Method, AutoOperateAnnotatedElement[]> methodParameterCaches = CollectionUtils.newWeakConcurrentMap();
    protected final ParameterNameFinder parameterNameFinder;
    protected final AnnotationFinder annotationFinder;
    protected final MethodBaseExpressionExecuteDelegate expressionExecuteDelegate;
    
    /**
     * Create a {@link MethodArgumentAutoOperateSupport} instance.
     *
     * @param elementResolver element handler
     * @param expressionExecuteDelegate expression evaluator delegate
     * @param parameterNameFinder parameter name finder
     * @param annotationFinder annotation finder
     */
    public MethodArgumentAutoOperateSupport(
        AutoOperateAnnotatedElementResolver elementResolver,
        MethodBaseExpressionExecuteDelegate expressionExecuteDelegate,
        ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        this.elementResolver = elementResolver;
        this.annotationFinder = annotationFinder;
        this.parameterNameFinder = parameterNameFinder;
        this.expressionExecuteDelegate = expressionExecuteDelegate;
    }

    /**
     * Before the method is called, process the input parameters
     * of the method according to the configuration of {@link ArgAutoOperate} or {@link AutoOperate} annotation.
     *
     * @param method method
     * @param args args
     */
    public final void beforeMethodInvoke(Method method, Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return;
        }
        // cache resolved parameters
        ArgAutoOperate methodLevelAnnotation = annotationFinder.findAnnotation(method, ArgAutoOperate.class);
        // fix https://gitee.com/opengoofy/crane4j/issues/I82EAC
        AutoOperateAnnotatedElement[] elements = CollectionUtils.computeIfAbsent(
            methodParameterCaches, method, name -> resolveParameters(methodLevelAnnotation, method)
        );
        if (elements == EMPTY_ELEMENTS) {
            return;
        }
        log.debug("process arguments for [{}]", method.getName());
        processArguments(method, args, elements);
    }

    /**
     * Processing method input arguments.
     *
     * @param method method
     * @param args args
     * @param autoOperateAnnotatedElements resolved elements
     */
    protected void processArguments(Method method, Object[] args, AutoOperateAnnotatedElement[] autoOperateAnnotatedElements) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // maybe not annotated element
            AutoOperateAnnotatedElement element = autoOperateAnnotatedElements[i];
            if (Objects.nonNull(element) && canApply(method, args, element.getAnnotation().condition())) {
                element.execute(arg);
            }
        }
    }

    /**
     * Analyze the annotations on methods and method parameters
     * to obtain the operation configuration of method parameters.
     *
     * @param argAutoOperate argAutoOperate
     * @param method method
     * @return operation configuration of method parameters
     */
    protected AutoOperateAnnotatedElement[] resolveParameters(@Nullable ArgAutoOperate argAutoOperate, Method method) {
        if (method.getParameterCount() < 1) {
            log.warn("cannot apply auto operate for method [{}], because it has no parameters", method);
            return EMPTY_ELEMENTS;
        }
        Map<String, AutoOperate> methodLevelAnnotations = Optional.ofNullable(argAutoOperate)
            .map(ArgAutoOperate::value)
            .map(Arrays::stream)
            .map(s -> s.collect(Collectors.toMap(AutoOperate::value, Function.identity())))
            .orElseGet(Collections::emptyMap);
        Map<String, Parameter> parameterMap = ReflectUtils.resolveParameterNames(parameterNameFinder, method);
        AutoOperateAnnotatedElement[] results = new AutoOperateAnnotatedElement[parameterMap.size()];
        int index = 0;
        for (Map.Entry<String, Parameter> entry : parameterMap.entrySet()) {
            // find the parameter level annotation first, then find the method level annotation
            String paramName = entry.getKey();
            Parameter param = entry.getValue();
            AutoOperate annotation = Optional
                .ofNullable(annotationFinder.getAnnotation(param, AutoOperate.class))
                .orElse(methodLevelAnnotations.get(paramName));
            results[index++] = Objects.isNull(annotation) ? null : elementResolver.resolve(param, annotation);
        }
        if (Stream.of(results).allMatch(Objects::isNull)) {
            log.warn("cannot apply auto operate for method [{}], because all parameters have no operation configuration", method);
            return EMPTY_ELEMENTS;
        }
        return results;
    }

    private boolean canApply(Method method, Object[] args, String condition) {
        return StringUtils.isEmpty(condition)
            || Boolean.TRUE.equals(expressionExecuteDelegate.execute(condition, Boolean.class, method, args, null));
    }
}
