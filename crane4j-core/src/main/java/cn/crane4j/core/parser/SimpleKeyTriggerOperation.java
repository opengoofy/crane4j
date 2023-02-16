package cn.crane4j.core.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link KeyTriggerOperation}的基本实现
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
     * 添加组别
     *
     * @param group 组别
     */
    public void putGroup(String group) {
        groups.add(group);
    }

    /**
     * 添加组别
     *
     * @param group 组别
     */
    public void removeGroup(String group) {
        groups.remove(group);
    }
}
