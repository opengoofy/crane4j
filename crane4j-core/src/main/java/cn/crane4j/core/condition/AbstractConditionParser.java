package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionType;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private final Set<AnnotatedElement> ignored = Collections.newSetFromMap(CollectionUtils.newWeakConcurrentMap());
    private final AnnotationFinder annotationFinder;
    protected final Class<A> annotationType;

    /**
     * Parse condition from a give element
     *
     * @param element element
     * @param operation operation
     * @return conditions
     */
    @NonNull
    @Override
    public final List<Condition> parse(
        AnnotatedElement element, KeyTriggerOperation operation) {
        if (ignored.contains(element)) {
            return Collections.emptyList();
        }
        List<Condition> results = doParse(element, operation);
        if (results.isEmpty()) {
            ignored.add(element);
            return Collections.emptyList();
        }
        return results;
    }

    @NonNull
    private List<Condition> doParse(AnnotatedElement element, KeyTriggerOperation operation) {
        Set<A> annotations = annotationFinder.getAllAnnotations(element, annotationType);
        if (annotations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Condition> results = new ArrayList<>();
        annotations.forEach(annotation -> {
            ConditionDescriptor descriptor = getConditionDescriptor(annotation);
            if (!canApply(descriptor.getOperationIds(), operation)) {
                return;
            }
            Condition condition = createCondition(element, annotation);
            if (Objects.isNull(condition)) {
                return;
            }
            ((AbstractCondition)condition)
                .setSort(descriptor.getSort())
                .setType(descriptor.getType());
            condition = descriptor.isNegate() ? condition.negate() : condition;
            results.add(condition);
        });
        return results;
    }

    private boolean canApply(String[] ids, KeyTriggerOperation operation) {
        return ArrayUtils.isEmpty(ids) || ArrayUtils.contains(ids, operation.getId());
    }

    /**
     * Create condition instance.
     *
     * @param element element
     * @param annotation annotation
     * @return condition instance
     */
    @Nullable
    protected abstract AbstractCondition createCondition(AnnotatedElement element, A annotation);

    /**
     * Get condition properties.
     *
     * @param annotation annotation
     * @return condition properties
     */
    @NonNull
    protected ConditionDescriptor getConditionDescriptor(A annotation) {
        return new ConditionDescriptor();
    }

    /**
     * A basic condition implementation.
     *
     * @author huangchengxing
     */
    @Accessors(chain = true)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    protected abstract static class AbstractCondition implements Condition {
        private ConditionType type = ConditionType.AND;
        private int sort = Integer.MAX_VALUE;
    }

    /**
     * Condition properties.
     *
     * @author huangchengxing
     */
    @Getter
    @SuperBuilder
    @NoArgsConstructor
    protected static class ConditionDescriptor {
        private static final String[] EMPTY_ID_ARRAY = new String[0];
        @Builder.Default
        private String[] operationIds = EMPTY_ID_ARRAY;
        @Builder.Default
        private ConditionType type = ConditionType.AND;
        @Builder.Default
        private boolean negate = false;
        @Builder.Default
        private int sort = Integer.MAX_VALUE;
    }
}
