package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Common template class for automatic operation based on {@link AutoOperate} annotation on method or method parameters.
 *
 * @author huangchengxing
 * @see AutoOperate
 */
@RequiredArgsConstructor
public class AutoOperateAnnotatedElementResolver {

    private final Crane4jGlobalConfiguration configuration;

    /**
     * Resolve the {@link AutoOperate} annotation on the element
     * and build {@link AutoOperateAnnotatedElement} for it according to its configuration.
     *
     * @param element element
     * @param annotation annotation
     * @return {@link AutoOperateAnnotatedElement}
     */
    public AutoOperateAnnotatedElement resolve(AnnotatedElement element, AutoOperate annotation) {
        MethodInvoker extractor = resolveExtractor(element, annotation);
        // prepare components for use
        BeanOperationParser parser = ConfigurationUtil.getOperationParser(configuration, annotation.parserName(), annotation.parser());
        BeanOperations beanOperations = parser.parse(annotation.type());
        BeanOperationExecutor executor = ConfigurationUtil.getOperationExecutor(configuration, annotation.executorName(), annotation.executor());
        // check groups
        Predicate<? super KeyTriggerOperation> filter = resolveFilter(annotation);
        return new AutoOperateAnnotatedElement(annotation, element, extractor, filter, beanOperations, executor);
    }

    private MethodInvoker resolveExtractor(AnnotatedElement element, AutoOperate annotation) {
        Class<?> type = resolveTypeForExtractor(element);
        String on = annotation.on();
        MethodInvoker extractor = (t, args) -> t;
        if (CharSequenceUtil.isNotEmpty(on)) {
            PropertyOperator propertyOperator = configuration.getPropertyOperator();
            extractor = propertyOperator.findGetter(type, on);
            Objects.requireNonNull(extractor, () -> CharSequenceUtil.format("cannot find getter for [{}] on [{}]", on, annotation.type()));
        }
        return extractor;
    }

    private static Class<?> resolveTypeForExtractor(AnnotatedElement element) {
        if (element instanceof Method) {
            return ((Method)element).getReturnType();
        } else if (element instanceof Parameter) {
            return ((Parameter)element).getType();
        } else {
            throw new Crane4jException("element must be a method or parameter");
        }
    }

    /**
     * Resolve group for {@link AutoOperate#includes()} and {@link AutoOperate#excludes()}.
     *
     * @param annotation annotation
     * @return actual include groups
     */
    protected static Predicate<? super KeyTriggerOperation> resolveFilter(AutoOperate annotation) {
        Set<String> excludes = CollectionUtils.newCollection(HashSet::new, annotation.excludes());
        Set<String> includes = CollectionUtils.newCollection(HashSet::new, annotation.includes());
        includes.removeAll(excludes);
        // nothing includes
        if (includes.isEmpty()) {
            return Grouped.noneMatch(annotation.excludes());
        }
        // nothing excludes
        if (excludes.isEmpty()) {
            return Grouped.anyMatch(annotation.includes());
        }
        // include or not exclude
        return t -> CollectionUtils.containsAny(includes, t.getGroups())
            || CollectionUtils.notContainsAny(excludes, t.getGroups());
    }
}
