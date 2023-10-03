package cn.crane4j.core.support.auto;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An abstract implementation of {@link AutoOperateAnnotatedElementResolver}.
 *
 * @author huangchengxing
 * @see ClassBasedAutoOperateAnnotatedElementResolver
 * @see MethodBasedAutoOperateAnnotatedElementResolver
 * @since 2.3.0
 */
@RequiredArgsConstructor
public abstract class AbstractAutoOperateAnnotatedElementResolver implements AutoOperateAnnotatedElementResolver {

    protected final Crane4jGlobalConfiguration configuration;

    /**
     * Resolve the {@link AutoOperate} annotation on the element
     * and build {@link AutoOperateAnnotatedElement} for it according to its configuration.
     *
     * @param element element
     * @param annotation annotation
     * @return {@link AutoOperateAnnotatedElement}
     */
    @Nullable
    @Override
    public AutoOperateAnnotatedElement resolve(@NonNull AnnotatedElement element, AutoOperate annotation) {
        Asserts.isNotNull(element, "element must not be null");
        Asserts.isNotNull(annotation, "annotation must not be null");
        MethodInvoker extractor = resolveExtractor(element, annotation);
        // prepare components for use
        BeanOperationParser parser = configuration.getBeanOperationsParser(annotation.parser(), annotation.parserType());
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(annotation.executor(), annotation.executorType());
        Predicate<? super KeyTriggerOperation> filter = resolveFilter(annotation);
        return createAutoOperateAnnotatedElement(element, annotation, parser, executor, extractor, filter);
    }

    /**
     * Create {@link AutoOperateAnnotatedElement} for the element.
     *
     * @param element element
     * @param annotation annotation
     * @param parser parser
     * @param executor executor
     * @param extractor extractor
     * @param filter filter
     * @return {@link AutoOperateAnnotatedElement}
     */
    @Nullable
    protected abstract AutoOperateAnnotatedElement createAutoOperateAnnotatedElement(
        AnnotatedElement element, AutoOperate annotation,
        BeanOperationParser parser, BeanOperationExecutor executor,
        MethodInvoker extractor, Predicate<? super KeyTriggerOperation> filter);

    /**
     * Resolve the extractor for {@link AutoOperate#value()}.
     *
     * @param element element
     * @param annotation annotation
     * @return extractor
     */
    protected abstract MethodInvoker resolveExtractor(AnnotatedElement element, AutoOperate annotation);

    /**
     * Resolve group for {@link AutoOperate#includes()} and {@link AutoOperate#excludes()}.
     *
     * @param annotation annotation
     * @return actual include groups
     */
    protected Predicate<? super KeyTriggerOperation> resolveFilter(AutoOperate annotation) {
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
