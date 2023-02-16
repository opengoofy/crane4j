package cn.crane4j.core.container;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>一个用于占位的空数据源容器，本身不提供任何数据，也不提供数据的注册功能。
 * 当一个装配操作指定使用该数据源容器时，实际上表示将使用操作对象本身作为数据源对象。
 *
 * @author huangchengxing
 */
public class EmptyContainer implements Container<Object> {

    public static final EmptyContainer INSTANCE = new EmptyContainer();
    public static final String NAMESPACE = "";

    /**
     * 获取数据源容器的命名空间，该值总是默认为空字符串
     *
     * @return 命名空间
     */
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    /**
     * 获取数据源，返回值总是默认为空集合
     *
     * @param keys keys
     * @return 按key值分组的数据源对象
     */
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        return Collections.emptyMap();
    }
}
