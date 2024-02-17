package cn.crane4j.core.condition;

import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.MultiMap;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;

/**
 * Condition parser.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.parser.ConditionalTypeHierarchyBeanOperationParser
 * @since 2.6.0
 */
public interface ConditionParser {

    /**
     * Parse condition from give element
     *
     * @param element element
     * @param operation operation
     * @return condition with id
     */
    @NonNull
    MultiMap<String, Condition> parse(AnnotatedElement element, KeyTriggerOperation operation);
}
