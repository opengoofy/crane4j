package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.executor.AssembleOperationHandler;

import java.util.Set;

/**
 * <p>根据指定的key触发的装配操作，表示根据指定key值获取数据源，
 * 并根据配置将数据源属性映射到目标对象属性上的一套流程配置信息。
 *
 * <p>该对象用于记录一次装配操作的需要四项配置：
 * <ol>
 *     <li>{@link #getKey()}: 目标对象的哪个字段是key字段；</li>
 *     <li>{@link #getContainer()}: 通过key字段值去哪个数据源获得对应的数据源对象；</li>
 *     <li>{@link #getPropertyMappings()}: 拿到数据源对象以后，要把里面的哪些属性塞到目标对象的哪些属性中；</li>
 *     <li>{@link #getAssembleOperationHandler()}: 要怎么塞这些属性值；</li>
 * </ol>
 *
 * @author huangchengxing
 * @see AssembleOperationHandler
 * @see Container
 * @see PropertyMapping
 * @see SimpleAssembleOperation
 */
public interface AssembleOperation extends KeyTriggerOperation {

    /**
     * 获取属性映射
     *
     * @return 属性映射
     */
    Set<PropertyMapping> getPropertyMappings();

    /**
     * 获取数据源容器
     *
     * @return 数据源容器
     */
    Container<?> getContainer();

    /**
     * 获取操作处理器
     *
     * @return 操作处理器
     */
    AssembleOperationHandler getAssembleOperationHandler();
    
}
