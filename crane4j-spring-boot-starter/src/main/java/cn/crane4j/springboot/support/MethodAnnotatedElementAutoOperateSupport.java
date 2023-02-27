package cn.crane4j.springboot.support;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common template class for automatic operation based on {@link AutoOperate} annotation on method or method parameters.
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see MethodBaseExpressionEvaluator
 * @see MethodArgumentAutoOperateAspect
 * @see MethodResultAutoOperateAspect
 */
@RequiredArgsConstructor
public class MethodAnnotatedElementAutoOperateSupport {

    private final Crane4jGlobalConfiguration configuration;
    private final MethodBaseExpressionEvaluator methodBaseExpressionEvaluator;

    /**
     * Check whether to apply the operation according to the expression evaluation result.
     *
     * @param args args
     * @param result result
     * @param method method
     * @param condition condition
     * @return boolean
     */
    public boolean checkSupport(Object[] args, Object result, Method method, String condition) {
        if (CharSequenceUtil.isEmpty(condition)) {
            return true;
        }
        MethodBaseExpressionEvaluator.MethodExecution methodContext = new MethodBaseExpressionEvaluator.MethodExecution(args, method, result);
        Boolean support = methodBaseExpressionEvaluator.execute(condition, Boolean.class, methodContext);
        return Objects.equals(Boolean.TRUE, support);
    }

    /**
     * Resolve the {@link AutoOperate} annotation on the element
     * and build {@link ResolvedElement} for it according to its configuration.
     *
     * @param element element
     * @param annotation annotation
     * @return {@link ResolvedElement}
     */
    public ResolvedElement resolveElement(AnnotatedElement element, AutoOperate annotation) {
        MethodInvoker extractor = resolveExtractor(element, annotation);
        // prepare components for use
        BeanOperationParser parser = ConfigurationUtil.getParser(configuration, annotation.parserName(), annotation.parser());
        BeanOperations beanOperations = parser.parse(annotation.type());
        BeanOperationExecutor executor = ConfigurationUtil.getOperationExecutor(configuration, annotation.executorName(), annotation.executor());
        // check groups
        Set<String> groups = resolveGroups(annotation);
        return new ResolvedElement(element, extractor, groups, beanOperations, executor);
    }

    private MethodInvoker resolveExtractor(AnnotatedElement element, AutoOperate annotation) {
        Class<?> type = resolveTypeForExtractor(element);
        String on = annotation.on();
        MethodInvoker extractor = (t, args) -> t;
        if (CharSequenceUtil.isNotEmpty(on)) {
            PropertyOperator propertyOperator = configuration.getPropertyOperator();
            extractor = propertyOperator.findGetter(type, on);
            Objects.requireNonNull(extractor, () -> CharSequenceUtil.format(
                "cannot find getter for [{}] on [{}]", on, annotation.type()
            ));
        }
        return extractor;
    }

    private static Class<?> resolveTypeForExtractor(AnnotatedElement element) {
        if (element instanceof Method) {
            return ((Method)element).getReturnType();
        } else if (element instanceof Parameter) {
            return ((Parameter)element).getType();
        } else {
            throw new IllegalArgumentException("element must be a method or parameter");
        }
    }

    /**
     * Resolve group for {@link AutoOperate#includes()} and {@link AutoOperate#excludes()}.
     *
     * @param annotation annotation
     * @return actual include groups
     */
    protected static Set<String> resolveGroups(AutoOperate annotation) {
        String[] includes = annotation.includes();
        String[] excludes = annotation.excludes();
        return Stream.of(includes)
            .filter(in -> !ArrayUtil.contains(excludes, in))
            .collect(Collectors.toSet());
    }

    /**
     * Resolved annotated element.
     */
    @Getter
    @RequiredArgsConstructor
    public static class ResolvedElement {
        private final AnnotatedElement element;
        private final MethodInvoker extractor;
        private final Set<String> groups;
        private final BeanOperations beanOperations;
        private final BeanOperationExecutor executor;
        public void execute(Object data) {
            Object target = extractor.invoke(data);
            if (Objects.nonNull(target)) {
                Predicate<? super KeyTriggerOperation> filter = groups.isEmpty() ?
                    op -> true : op -> groups.contains(op.getKey());
                executor.execute(CollectionUtils.adaptObjectToCollection(target), beanOperations, filter);
            }
        }
    }
}
