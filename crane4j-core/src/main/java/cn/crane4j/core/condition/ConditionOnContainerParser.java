package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnContainer;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.stream.Stream;

/**
 * A parser to process {@link ConditionOnContainer} annotation.
 *
 * @author huangchengxing
 */
public class ConditionOnContainerParser extends AbstractConditionParser<ConditionOnContainer> {

    private final ContainerManager containerManager;

    public ConditionOnContainerParser(
        AnnotationFinder annotationFinder, ContainerManager containerManager) {
        super(annotationFinder, ConditionOnContainer.class);
        this.containerManager = containerManager;
    }

    /**
     * Get condition properties.
     *
     * @param annotation annotation
     * @return condition properties
     */
    @NonNull
    @Override
    protected ConditionDescriptor getConditionDescriptor(ConditionOnContainer annotation) {
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
    protected AbstractCondition createCondition(AnnotatedElement element, ConditionOnContainer annotation) {
        return new ContainerCondition(containerManager, annotation.value());
    }

    @RequiredArgsConstructor
    private static class ContainerCondition extends AbstractCondition {
        private final ContainerManager containerManager;
        private final String[] namespaces;
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            return Stream.of(namespaces)
                .allMatch(containerManager::containsContainer);
        }
    }
}
