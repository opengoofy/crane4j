package cn.crane4j.core.parser.operation;

import cn.crane4j.core.condition.Condition;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

/**
 * Basic implementation of {@link KeyTriggerOperation}.
 *
 * @author huangchengxing
 * @see SimpleAssembleOperation
 * @see TypeFixedDisassembleOperation
 * @see TypeDynamitedDisassembleOperation
 */
@SuperBuilder
@Getter
public class SimpleKeyTriggerOperation implements KeyTriggerOperation {

    @Nullable
    private final Object source;
    @Nullable
    @Builder.Default
    private String id = null;
    private final String key;
    @Singular
    private final Set<String> groups;
    @Builder.Default
    private int sort = Integer.MAX_VALUE;
    @Nullable
    @Setter
    private Condition condition;
}
