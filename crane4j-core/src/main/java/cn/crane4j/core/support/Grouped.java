package cn.crane4j.core.support;

import cn.hutool.core.util.ArrayUtil;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 表示具有组别的对象
 *
 * @author huangchengxing
 */
public interface Grouped {

    /**
     * <p>对象同时属于{@code groups}中的所有分组，比如：
     * <table>
     *     <tr><td>输入  </td><td>检验   </td><td>结果 </td></tr>
     *     <tr><td>[ ]   </td><td>any   </td><td>true </td></tr>
     *     <tr><td>[a]   </td><td>[a]   </td><td>true </td></tr>
     *     <tr><td>[a]   </td><td>[a, b]</td><td>false</td></tr>
     *     <tr><td>[a, b]</td><td>[a, b]</td><td>true </td></tr>
     *     <tr><td>[a, b]</td><td>[b]   </td><td>false</td></tr>
     * </table>
     *
     * @param groups 分组
     * @return cn.net.nova.component.chain.group.TagFilter
     */
    static Predicate<Grouped> allMatch(String... groups) {
        return ArrayUtil.isEmpty(groups) ?
            alwaysMatch() : t -> Stream.of(groups).allMatch(t::isBelong);
    }

    /**
     * <p>对象不属于{@code groups}中的任意分组，比如：
     * <table>
     *     <tr><td>输入  </td><td>检验   </td><td>结果 </td></tr>
     *     <tr><td>[ ]   </td><td>any   </td><td>true </td></tr>
     *     <tr><td>[a, b]</td><td>[a, b]</td><td>false</td></tr>
     *     <tr><td>[a, b]</td><td>[a]   </td><td>false</td></tr>
     *     <tr><td>[a, b]</td><td>[c]   </td><td>true </td></tr>
     * </table>
     *
     * @param groups 分组
     * @return cn.net.nova.component.chain.group.TagFilter
     */
    static Predicate<Grouped> noneMatch(String... groups) {
        return ArrayUtil.isEmpty(groups) ?
            alwaysMatch() : t -> Stream.of(groups).noneMatch(t::isBelong);
    }

    /**
     * <p>对象属于{@code groups}中的任意分组，比如：
     * <table>
     *     <tr><td>输入  </td><td>检验   </td><td>结果 </td></tr>
     *     <tr><td>[ ]   </td><td>any   </td><td>false</td></tr>
     *     <tr><td>[a, b]</td><td>[a, b]</td><td>true </td></tr>
     *     <tr><td>[a, b]</td><td>[a]   </td><td>true </td></tr>
     *     <tr><td>[a, b]</td><td>[c]   </td><td>false</td></tr>
     * </table>
     *
     * @param groups 分组
     * @return cn.net.nova.component.chain.group.TagFilter
     */
    static Predicate<Grouped> anyMatch(String... groups) {
        return ArrayUtil.isEmpty(groups) ?
            alwaysNoneMatch() : t -> Stream.of(groups).anyMatch(t::isBelong);
    }

    /**
     * 对象总是属于任何分组
     *
     * @return cn.net.nova.component.chain.group.TagFilter
     */
    static Predicate<Grouped> alwaysMatch() {
        return grouped -> true;
    }

    /**
     * 对象总是不属于任何分组
     *
     * @return cn.net.nova.component.chain.group.TagFilter
     */
    static Predicate<Grouped> alwaysNoneMatch() {
        return grouped -> false;
    }

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
