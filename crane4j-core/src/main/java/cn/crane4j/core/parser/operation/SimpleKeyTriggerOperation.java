package cn.crane4j.core.parser.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Basic implementation of {@link KeyTriggerOperation}.
 *
 * @author huangchengxing
 * @see SimpleAssembleOperation
 * @see TypeFixedDisassembleOperation
 * @see TypeDynamitedDisassembleOperation
 */
@Getter
@RequiredArgsConstructor
public class SimpleKeyTriggerOperation implements KeyTriggerOperation {

    private final String key;
    private final Set<String> groups = new LinkedHashSet<>();
    private final int sort;

    /**
     * Add group.
     *
     * @param group group
     */
    public void putGroup(String group) {
        groups.add(group);
    }

    /**
     * Add group.
     *
     * @param group group
     */
    public void removeGroup(String group) {
        groups.remove(group);
    }
}
