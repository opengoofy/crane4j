package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionType;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Sorted;

/**
 * A condition to check whether apply the operation or not to the target.
 *
 * @author huangchengxing
 * @see Condition
 * @since 2.6.0
 */
public interface Condition extends Sorted {

    /**
     * Get the type of multi conditions.
     *
     * @return condition type
     */
    default ConditionType getType() {
        return ConditionType.AND;
    }

    /**
     * Whether the operation should be applied to the target.
     *
     * @param target the target to be checked
     * @param operation the operation to be checked
     * @return true if the operation should be applied to the target, otherwise false
     */
    boolean test(Object target, KeyTriggerOperation operation);

    /**
     * Returns a composed condition that represents a short-circuiting logical AND of this condition and another.
     *
     * @param other a condition that will be logical-AND with this condition
     * @return a composed condition that represents the short-circuiting logical AND of this condition and the other condition
     */
    default Condition and(Condition other) {
        return (target, operation) ->
            test(target, operation) && other.test(target, operation);
    }

    /**
     * Returns a composed condition that represents a short-circuiting logical OR of this condition and another.
     *
     * @param other a condition that will be logical-OR with this condition
     * @return a composed condition that represents the short-circuiting logical OR of this condition and the other condition
     */
    default Condition or(Condition other) {
        return (target, operation) ->
            test(target, operation) || other.test(target, operation);
    }

    /**
     * Returns a condition that represents the logical negation of this condition.
     *
     * @return a condition that represents the logical negation of this condition
     */
    default Condition negate() {
        return (target, operation) -> !test(target, operation);
    }
}
