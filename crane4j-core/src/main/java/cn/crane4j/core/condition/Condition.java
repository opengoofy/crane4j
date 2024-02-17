package cn.crane4j.core.condition;

import cn.crane4j.core.parser.operation.KeyTriggerOperation;

/**
 * A condition to check whether apply the operation or not to the target.
 *
 * @author huangchengxing
 * @see Condition
 * @since 2.6.0
 */
public interface Condition {

    /**
     * Whether the operation should be applied to the target.
     *
     * @param target the target to be checked
     * @param operation the operation to be checked
     * @return true if the operation should be applied to the target, otherwise false
     */
    boolean test(Object target, KeyTriggerOperation operation);
}
