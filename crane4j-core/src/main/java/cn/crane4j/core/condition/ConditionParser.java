package cn.crane4j.core.condition;

import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Condition parser.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.parser.ConditionalTypeHierarchyBeanOperationParser
 * @since 2.6.0
 */
public interface ConditionParser {

    /**
     * Parse condition from a give element
     *
     * @param element element
     * @param operation operation
     * @return condition with id
     */
    @NonNull
    List<Condition> parse(AnnotatedElement element, KeyTriggerOperation operation);
}
