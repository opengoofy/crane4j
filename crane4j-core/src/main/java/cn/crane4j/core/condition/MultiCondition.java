package cn.crane4j.core.condition;

import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.Asserts;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * Multi condition.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
@RequiredArgsConstructor
public abstract class MultiCondition implements Condition {

    protected final Condition[] conditions;

    /**
     * Create a new and condition with given conditions,
     * all conditions must be true to make the new condition true.
     *
     * @param conditions conditions
     * @return new condition
     */
    public static Condition and(Condition... conditions) {
        Asserts.isNotEmpty(conditions, "conditions must not empty!");
        return new And(conditions);
    }

    /**
     * Create a new and condition with given conditions,
     * any condition is true to make the new condition true.
     *
     * @param conditions conditions
     * @return new condition
     */
    public static Condition or(Condition... conditions) {
        Asserts.isNotEmpty(conditions, "conditions must not empty!");
        return new Or(conditions);
    }

    private static class And extends MultiCondition {
        public And(Condition[] conditions) {
            super(conditions);
        }
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            return Stream.of(conditions).allMatch(c -> c.test(target, operation));
        }
    }

    private static class Or extends MultiCondition {
        public Or(Condition[] conditions) {
            super(conditions);
        }
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            return Stream.of(conditions).anyMatch(c -> c.test(target, operation));
        }
    }
}
