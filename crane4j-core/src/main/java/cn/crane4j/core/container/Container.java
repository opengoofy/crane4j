package cn.crane4j.core.container;

import java.util.Collection;
import java.util.Map;

/**
 * 用于存放/提供数据对象的源容器，即代表可用于完成装配操作的数据源。
 * 任何可以接受key值集合并返回按key值分组的Map集合对象/方法都可以作为数据源。
 *
 * @author huangchengxing
 * @see ConstantContainer
 * @see LambdaContainer
 * @see MethodInvokerContainer
 */
public interface Container<K> {

    /**
     * 获取一个空的数据源容器，当一个装配操作指定使用该数据源容器时，
     * 将使用操作对象本身作为数据源对象。
     *
     * @return 数据源容器
     * @see EmptyContainer
     */
    @SuppressWarnings("unchecked")
    static <K> Container<K> empty() {
        return (Container<K>)EmptyContainer.INSTANCE;
    }

    /**
     * 获取数据源容器的命名空间，该值应当全局唯一
     *
     * @return 命名空间
     */
    String getNamespace();

    /**
     * 输入一批key值，返回按key值分组的数据源对象
     *
     * @param keys keys
     * @return 按key值分组的数据源对象
     */
    Map<K, ?> get(Collection<K> keys);

}
