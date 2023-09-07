package cn.crane4j.core.support.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Decorated property operator.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public interface DecoratedPropertyOperator extends PropertyOperator {

    /**
     * Get the original PropertyOperator
     *
     * @return {@link PropertyOperator} instance
     */
    @NonNull
    PropertyOperator getPropertyOperator();

    /**
     * Set property operator.
     *
     * @param propertyOperator property operator
     */
    void setPropertyOperator(@NonNull PropertyOperator propertyOperator);
}
