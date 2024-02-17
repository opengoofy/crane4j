package cn.crane4j.core.parser.operation;

import cn.crane4j.core.condition.Condition;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.Sorted;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>For operations triggered by a specific key. <br />
 * operations can be sorted according to the size of {@link #getSort()} to change the execution order,
 * and the group to which the operation belongs can be obtained through {@link #getGroups()}.
 *
 * @author huangchengxing
 * @see SimpleKeyTriggerOperation
 */
public interface KeyTriggerOperation extends Grouped, Sorted {

    /**
     * Get configuration source.
     *
     * @return source
     * @since 2.6.0
     */
    @Nullable
    Object getSource();

    /**
     * Get operation id.
     *
     * @return id
     * @since 2.6.0
     */
    @Nullable
    String getId();
    
    /**
     * Get key field name.
     *
     * @return key field name
     */
    String getKey();

    /**
     * Get operation predicate.
     *
     * @return operation predicate
     * @since 2.6.0
     */
    @Nullable Condition getCondition();

    /**
     * Set operation predicate.
     *
     * @param condition operation predicate
     * @since 2.6.0
     */
    void setCondition(Condition condition);
}
