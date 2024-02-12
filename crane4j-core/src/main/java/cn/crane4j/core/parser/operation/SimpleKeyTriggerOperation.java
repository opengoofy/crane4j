package cn.crane4j.core.parser.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    @Setter
    @Nullable
    private String id;
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
