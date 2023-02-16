package cn.crane4j.core.container;

import cn.crane4j.core.support.Sorted;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 用于根据方法创建数据源容器的工厂
 *
 * @author huangchengxing
 * @see DefaultMethodContainerFactory
 * @see CacheableMethodContainerFactory
 */
public interface MethodContainerFactory extends Sorted {

    /**
     * 是否支持处理该方法
     *
     * @param source 方法的调用对象
     * @param method 方法
     * @return 是否
     */
    boolean support(Object source, Method method);

    /**
     * 获取方法数据源
     *
     * @param source 方法的调用对象
     * @param method 方法
     * @return 方法数据源容器
     */
    List<Container<Object>> get(Object source, Method method);
}
