package cn.crane4j.core.parser;

import cn.crane4j.annotation.condition.ConditionType;
import cn.crane4j.core.condition.Condition;
import cn.crane4j.core.condition.ConditionParser;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * An extension of {@link TypeHierarchyBeanOperationParser} that supports condition parsing.
 *
 * @author huangchengxing
 * @see ConditionParser
 * @see Condition
 * @since 2.6.0
 */
@Getter
public class ConditionalTypeHierarchyBeanOperationParser
    extends TypeHierarchyBeanOperationParser {

    /**
     * condition parsers
     */
    private final List<ConditionParser> conditionParsers = new ArrayList<>();

    /**
     * Register condition parser
     *
     * @param conditionParser conditionParser
     */
    public void registerConditionParser(ConditionParser conditionParser) {
        Asserts.isNotNull(conditionParser, "conditionParser must not null");
        conditionParsers.remove(conditionParser);
        conditionParsers.add(conditionParser);
    }

    /**
     * Parse {@link BeanOperations} from {@code source} if necessary.
     *
     * @param source source
     * @return operations from source, it may come from cache
     */
    @Override
    protected BeanOperations doResolveToOperations(AnnotatedElement source) {
        BeanOperations operations = super.doResolveToOperations(source);
        MultiMap<AnnotatedElement, KeyTriggerOperation> sourceWithOperations = MultiMap.arrayListMultimap();
        Stream.of(operations.getDisassembleOperations(), operations.getAssembleOperations())
            .flatMap(Collection::stream)
            .filter(op -> op.getSource() instanceof AnnotatedElement)
            .forEach(op -> sourceWithOperations.put((AnnotatedElement)op.getSource(), op));
        sourceWithOperations.asMap().forEach(this::process);
        return operations;
    }

    protected void process(
        AnnotatedElement element, Collection<KeyTriggerOperation> operations) {
        MultiMap<String, Condition> conditions = MultiMap.arrayListMultimap();
        operations.forEach(operation -> conditionParsers.stream()
            .map(parser -> parser.parse(element, operation))
            .filter(mm -> !mm.isEmpty())
            .forEach(conditions::putAll)
        );
        operations.forEach(op -> {
            Collection<Condition> cs = conditions.get(op.getId());
            if (!cs.isEmpty()) {
                bindConditionToOperation(cs, op);
            }
        });
    }

    protected void bindConditionToOperation(
        Collection<Condition> conditions, KeyTriggerOperation operation) {
        Condition condition = conditions.size() > 1 ?
            conditions.stream()
                .sorted(Comparator.comparing(Sorted::getSort))
                .reduce(null, this::merge) :
            CollectionUtils.getFirstNotNull(conditions);
        operation.setCondition(condition);
    }

    private Condition merge(@Nullable Condition prev, Condition next) {
        if (prev == null) {
            return next;
        }
        return prev.getType() == ConditionType.AND ?
            prev.and(next) : prev.or(next);
    }
}
