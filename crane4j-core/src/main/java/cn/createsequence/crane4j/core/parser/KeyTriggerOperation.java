package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.support.Grouped;
import cn.createsequence.crane4j.core.support.Sorted;

/**
 * 由特定key触发的操作，操作之间支持按{@link #getSort()}大小排序从而改变执行顺序，
 * 并支持通过{@link #getGroups()}获得操作所属的组别。
 *
 * @author huangchengxing
 * @see SimpleKeyTriggerOperation
 */
public interface KeyTriggerOperation extends Grouped, Sorted {

    /**
     * 获取Key值
     *
     * @return key值
     */
    String getKey();
}
