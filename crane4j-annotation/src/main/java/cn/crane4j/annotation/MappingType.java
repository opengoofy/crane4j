package cn.crane4j.annotation;

import java.util.Map;

/**
 * The mapping relationship between the object returned by the method and the target object.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.container.MethodInvokerContainer;
 * @see ContainerMethod#type()
 */
public enum MappingType {

    /**
     * <p>Instead of mapping by key,
     * combine key and value directly into a Map collection in sequence,
     * such as {@code Collections#zip}.
     *
     * <p>When using this type,
     * the return {@link ContainerMethod#resultKey()} and {@link ContainerMethod#resultType()} are <strong>ignored</strong>.
     *
     * @see 2.4.0
     */
    NONE,

    /**
     * <p>The return value of the method is already a {@link Map} set grouped by the key value.
     * No further conversion is required according to the key value.
     *
     * <p>When using this type,
     * the return {@link ContainerMethod#resultKey()} and {@link ContainerMethod#resultType()} are <strong>ignored</strong>.
     */
    MAPPED,

    /**
     * <p>One key corresponds to one data source object, that is,
     * in the returned data source object collection,
     * multiple data source objects correspond to the same key value.
     *
     * <p>When using this type,
     * the type which specified by {@link ContainerMethod#resultType()} <strong>must be {@code Map} or java bean</strong>.
     */
    ONE_TO_ONE,

    /**
     * <p>One key corresponds to multiple data source objects,
     * that is, one data source object only corresponds
     * to the same key value in the returned data source object collection.
     *
     * <p>When using this type,
     * the type which specified by {@link ContainerMethod#resultType()} <strong>must be {@code Map} or java bean</strong>.
     */
    ONE_TO_MANY;
}
