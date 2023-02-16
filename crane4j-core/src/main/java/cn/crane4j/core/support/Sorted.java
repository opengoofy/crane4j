package cn.crane4j.core.support;

/**
 * 表示允许根据排序值从小到大排序的对象
 *
 * @author huangchengxing
 */
public interface Sorted {

    /**
     * 获取排序值，越小越优先执行
     *
     * @return 排序值
     */
    default int getSort() {
        return Integer.MAX_VALUE;
    }
}
