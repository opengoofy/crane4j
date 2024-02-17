package cn.crane4j.core.condition;

import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A condition parser implementation to process annotation based condition.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
@RequiredArgsConstructor
public abstract class AbstractConditionParser<A extends Annotation> implements ConditionParser {

    private static final String[] EMPTY_ID_ARRAY = new String[0];
    private final Set<AnnotatedElement> ignored = Collections.newSetFromMap(CollectionUtils.newWeakConcurrentMap());
    private final AnnotationFinder annotationFinder;
    protected final Class<A> annotationType;

    /**
     * Parse condition from give element
     *
     * @param element element
     * @param operation operation
     * @return condition with id
     */
    @NonNull
    @Override
    public final MultiMap<String, Condition> parse(
        AnnotatedElement element, KeyTriggerOperation operation) {
        if (ignored.contains(element)) {
            return MultiMap.emptyMultiMap();
        }
        MultiMap<String, Condition> results = doParse(element, operation);
        if (results.isEmpty()) {
            ignored.add(element);
            return MultiMap.emptyMultiMap();
        }
        return results;
    }

    @NonNull
    private MultiMap<String, Condition> doParse(AnnotatedElement element, KeyTriggerOperation operation) {
        Set<A> annotations = annotationFinder.getAllAnnotations(element, annotationType);
        if (annotations.isEmpty()) {
            return MultiMap.emptyMultiMap();
        }
        MultiMap<String, Condition> results = MultiMap.arrayListMultimap();
        annotations.forEach(annotation -> {
            String[] ids = determineIds(operation, annotation);
            if (Objects.isNull(ids)) {
                return;
            }
            Condition condition = createCondition(element, annotation);
            if (Objects.isNull(condition)) {
                return;
            }
            for (String id : ids) {
                results.put(id, condition);
            }
        });
        return results;
    }

    @Nullable
    private String[] determineIds(KeyTriggerOperation operation, A annotation) {
        String[] ids = getOperationIds(annotation);
        if (ArrayUtils.isNotEmpty(ids)) {
            // this condition should apply to current operation ?
            return ArrayUtils.contains(ids, operation.getId()) ? null : ids;
        }
        return new String[] { operation.getId() };
    }

    /**
     * The id of the operations to apply this condition.
     *
     * @param annotation annotation
     * @return ids
     */
    @Nullable
    protected String[] getOperationIds(A annotation) {
        return EMPTY_ID_ARRAY;
    }

    /**
     * Create condition instance.
     *
     * @param element element
     * @param annotation annotation
     * @return condition instance
     */
    @Nullable
    protected abstract Condition createCondition(AnnotatedElement element, A annotation);
}
