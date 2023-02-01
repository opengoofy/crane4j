package cn.createsequence.crane4j.core.support;

import java.util.Collections;
import java.util.Set;

/**
 * 表示具有组别的对象
 *
 * @author huangchengxing
 */
public interface Grouped {

    /**
     * 获取所属的组别
     *
     * @return 组别
     */
    default Set<String> getGroups() {
        return Collections.emptySet();
    }

    /**
     * 当前对象是否属于指定组别
     *
     * @param group 组别
     * @return 是否
     */
    default boolean isBelong(String group) {
        return getGroups().contains(group);
    }
}
