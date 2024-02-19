package cn.crane4j.annotation.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The type of multi conditions.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
@Getter
@RequiredArgsConstructor
public enum ConditionType {

    /**
     * multi conditions must be satisfied at the same time.
     */
    AND,

    /**
     * at least one condition must be satisfied.
     */
    OR;
}
