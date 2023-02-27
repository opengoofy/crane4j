package cn.crane4j.core.support;

import cn.hutool.core.util.ArrayUtil;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents objects that can be grouped by a specific name.
 *
 * @author huangchengxing
 */
public interface Grouped {

    /**
     * <p>The object belongs to all the groups in {@code groups}, for example:
     * <table>
     *     <tr><td>input  </td><td>test  </td><td>result</td></tr>
     *     <tr><td>[ ]    </td><td>any   </td><td>true  </td></tr>
     *     <tr><td>[a]    </td><td>[a]   </td><td>true  </td></tr>
     *     <tr><td>[a]    </td><td>[a, b]</td><td>false </td></tr>
     *     <tr><td>[a, b] </td><td>[a, b]</td><td>true  </td></tr>
     *     <tr><td>[a, b] </td><td>[b]   </td><td>false </td></tr>
     * </table>
     *
     * @param groups groups
     * @return predicate
     */
    static Predicate<Grouped> allMatch(String... groups) {
        return ArrayUtil.isEmpty(groups) ?
            alwaysMatch() : t -> Stream.of(groups).allMatch(t::isBelong);
    }

    /**
     * <p>The object does not belong to any group in {@code groups}, for example:
     * <table>
     *     <tr><td>input  </td><td>test  </td><td>result</td></tr>
     *     <tr><td>[ ]    </td><td>any   </td><td>true  </td></tr>
     *     <tr><td>[a, b] </td><td>[a, b]</td><td>false </td></tr>
     *     <tr><td>[a, b] </td><td>[a]   </td><td>false </td></tr>
     *     <tr><td>[a, b] </td><td>[c]   </td><td>true  </td></tr>
     * </table>
     *
     * @param groups groups
     * @return predicate
     */
    static Predicate<Grouped> noneMatch(String... groups) {
        return ArrayUtil.isEmpty(groups) ?
            alwaysMatch() : t -> Stream.of(groups).noneMatch(t::isBelong);
    }

    /**
     * <p>The object belongs to any group in {@code groups}, for example:
     * <table>
     *     <tr><td>input  </td><td>test  </td><td>result</td></tr>
     *     <tr><td>[ ]    </td><td>any   </td><td>false </td></tr>
     *     <tr><td>[a, b] </td><td>[a, b]</td><td>true  </td></tr>
     *     <tr><td>[a, b] </td><td>[a]   </td><td>true  </td></tr>
     *     <tr><td>[a, b] </td><td>[c]   </td><td>false </td></tr>
     * </table>
     *
     * @param groups groups
     * @return predicate
     */
    static Predicate<Grouped> anyMatch(String... groups) {
        return ArrayUtil.isEmpty(groups) ?
            alwaysNoneMatch() : t -> Stream.of(groups).anyMatch(t::isBelong);
    }

    /**
     * Objects always belong to any group.
     *
     * @return predicate
     */
    static Predicate<Grouped> alwaysMatch() {
        return grouped -> true;
    }

    /**
     * The object always does not belong to any group.
     *
     * @return predicate
     */
    static Predicate<Grouped> alwaysNoneMatch() {
        return grouped -> false;
    }

    /**
     * Get group names.
     *
     * @return group names
     */
    default Set<String> getGroups() {
        return Collections.emptySet();
    }

    /**
     * Whether the current object belongs to the specified group.
     *
     * @param group group
     * @return boolean
     */
    default boolean isBelong(String group) {
        return getGroups().contains(group);
    }
}
