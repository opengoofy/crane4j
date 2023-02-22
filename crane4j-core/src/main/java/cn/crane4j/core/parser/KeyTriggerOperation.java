package cn.crane4j.core.parser;

import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.Sorted;

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
     * Get key field name.
     *
     * @return key field name
     */
    String getKey();
}
