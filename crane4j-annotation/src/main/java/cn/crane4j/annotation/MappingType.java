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
     * The return value of the method is already a {@link Map} set grouped by the key value.
     * No further conversion is required according to the key value.
     */
    MAPPED,

    /**
     * One key corresponds to one data source object, that is,
     * in the returned data source object collection,
     * multiple data source objects correspond to the same key value.
     */
    ONE_TO_ONE,

    /**
     * One key corresponds to multiple data source objects,
     * that is, one data source object only corresponds
     * to the same key value in the returned data source object collection.
     */
    ONE_TO_MANY;
}
