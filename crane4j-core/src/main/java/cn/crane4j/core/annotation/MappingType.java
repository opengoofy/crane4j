package cn.crane4j.core.annotation;

import cn.crane4j.core.container.MethodInvokerContainer;

import java.util.Map;

/**
 * 方法类型
 *
 * @author huangchengxing
 * @see MethodInvokerContainer
 */
public enum MappingType {

    /**
     * 方法的返回值已经是按key值分组的{@link Map}集合，无需根据key值再进行转换
     */
    MAPPED,

    /**
     * 一个key对应一个数据源对象，即返回的数据源对象集合中，多个数据源对象对应同一个key值
     */
    ONE_TO_ONE,

    /**
     * 一个key对应多个数据源对象，即返回的数据源对象集合中，一个数据源对象仅对应同一个key值
     */
    ONE_TO_MANY;
}
