package cn.crane4j.extension.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.expression.MethodBaseExpressionEvaluatorDelegate;
import cn.crane4j.extension.support.ParameterNameFinder;
import cn.crane4j.extension.util.MethodUtils;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Method argument auto operate support.
 *
 * @author huangchengxing
 */
@Slf4j
public class MethodArgumentAutoOperateSupport extends MethodAnnotatedElementAutoOperateSupport {

    protected static final ResolvedElement[] EMPTY_ELEMENTS = new ResolvedElement[0];
    protected final Map<String, ResolvedElement[]> methodParameterCaches = CollectionUtils.newWeakConcurrentMap();
    protected final ParameterNameFinder parameterNameFinder;
    protected final AnnotationFinder annotationFinder;
    
    /**
     * Create a {@link MethodArgumentAutoOperateSupport} instance.
     *
     * @param configuration configuration
     * @param methodBaseExpressionEvaluatorDelegate method base expression evaluator delegate
     * @param parameterNameFinder parameter name finder
     * @param annotationFinder annotation finder
     */
    public MethodArgumentAutoOperateSupport(
        Crane4jGlobalConfiguration configuration,
        MethodBaseExpressionEvaluatorDelegate methodBaseExpressionEvaluatorDelegate,
        ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        super(configuration, methodBaseExpressionEvaluatorDelegate);
        this.annotationFinder = annotationFinder;
        this.parameterNameFinder = parameterNameFinder;
    }

    /**
     * Before the method is called, process the input parameters
     * of the method according to the configuration of {@link ArgAutoOperate} annotation.
     *
     * @param annotation annotation
     * @param method method
     * @param args args
     */
    public final void beforeMethodInvoke(ArgAutoOperate annotation, Method method, Object[] args) {
        // has annotation?
        if (Objects.isNull(annotation)) {
            return;
        }
        // has any arguments?
        if (ArrayUtil.isEmpty(args)) {
            return;
        }
        // cache resolved parameters
        ResolvedElement[] elements = MapUtil.computeIfAbsent(
            methodParameterCaches, method.getName(), name -> resolveParameters(annotation, method)
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
     * @param resolvedElements resolved elements
     */
    protected void processArguments(Method method, Object[] args, ResolvedElement[] resolvedElements) {
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
                e.printStackTrace();
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
    protected ResolvedElement[] resolveParameters(ArgAutoOperate argAutoOperate, Method method) {
        Map<String, Parameter> parameterMap = MethodUtils.resolveParameterNames(parameterNameFinder, method);
        Map<String, AutoOperate> methodLevelAnnotations = Stream.of(argAutoOperate.value())
            .collect(Collectors.toMap(AutoOperate::value, Function.identity()));

        ResolvedElement[] results = new ResolvedElement[parameterMap.size()];
        int index = 0;
        for (Map.Entry<String, Parameter> entry : parameterMap.entrySet()) {
            // find the parameter first, then the method
            String paramName = entry.getKey();
            Parameter param = entry.getValue();
            AutoOperate annotation = Optional
                .ofNullable(annotationFinder.findAnnotation(param, AutoOperate.class))
                .orElse(methodLevelAnnotations.get(paramName));
            // resolve annotation
            ResolvedElement element = Objects.isNull(annotation) ?
                EmptyElement.INSTANCE : resolveElement(param, annotation);
            results[index++] = element;
        }
        return results;
    }

    /**
     * Empty implementation of {@link ResolvedElement}, only for placeholder.
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
