package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnTargetType;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.Asserts;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A parser to process {@link ConditionOnTargetType} annotation.
 *
 * @author huangchengxing
 */
public class ConditionOnTargetTypeParser extends AbstractConditionParser<ConditionOnTargetType> {

    public ConditionOnTargetTypeParser(
        AnnotationFinder annotationFinder) {
        super(annotationFinder, ConditionOnTargetType.class);
    }

    /**
     * Get condition properties.
     *
     * @param annotation annotation
     * @return condition properties
     */
    @NonNull
    @Override
    protected ConditionDescriptor getConditionDescriptor(ConditionOnTargetType annotation) {
        return ConditionDescriptor.builder()
            .boundOperationIds(annotation.id())
            .type(annotation.type())
            .sort(annotation.sort())
            .negate(annotation.negate())
            .build();
    }

    /**
     * Create condition instance.
     *
     * @param element    element
     * @param annotation annotation
     * @return condition instance
     */
    @Nullable
    @Override
    protected AbstractCondition createCondition(AnnotatedElement element, ConditionOnTargetType annotation) {
        Asserts.isNotEmpty(annotation.value(), "The expected value is not specified in the @{} on {}", annotationType.getSimpleName(), element);
        return annotation.strict() ?
            new StrictlyTargetTypeCondition(annotation.value()) : new TargetTypeCondition(annotation.value());
    }

    @RequiredArgsConstructor
    private static class TargetTypeCondition extends AbstractCondition {
        private final Class<?>[] types;
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            return Stream.of(types)
                .allMatch(type -> type.isInstance(target));
        }
    }

    @RequiredArgsConstructor
    private static class StrictlyTargetTypeCondition extends AbstractCondition {
        private final Class<?>[] types;
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            Class<?> targetType = target.getClass();
            return Stream.of(types)
                .allMatch(type -> Objects.equals(type, targetType));
        }
    }
}
