package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnExpression;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.expression.ExpressionContext;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.util.Asserts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;

/**
 * A parser to process {@link ConditionOnExpression} annotation.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
@Slf4j
public class ExpressionConditionParser
    extends AbstractConditionParser<ConditionOnExpression> {

    public static final String VAR_TARGET = "target";
    public static final String VAR_OPERATION = "operation";

    private final ExpressionEvaluator expressionEvaluator;
    private final ContextFactory contextFactory;

    public ExpressionConditionParser(
        AnnotationFinder annotationFinder,
        ExpressionEvaluator expressionEvaluator, ContextFactory contextFactory) {
        super(annotationFinder, ConditionOnExpression.class);
        this.expressionEvaluator = expressionEvaluator;
        this.contextFactory = contextFactory;
    }

    /**
     * Get id of operation which to bound.
     *
     * @param annotation annotation
     * @return ids
     */
    @Nullable
    @Override
    protected String[] getOperationIds(ConditionOnExpression annotation) {
        return annotation.id();
    }

    /**
     * create condition instance.
     *
     * @param element    element
     * @param annotation annotation
     * @return condition instance
     */
    @Nullable
    @Override
    protected Condition createCondition(AnnotatedElement element, ConditionOnExpression annotation) {
        String expression = annotation.value();
        Asserts.isNotEmpty(expression, "not specified expression to apply with annotation @{} on {}", annotationType.getSimpleName(), element);
        return new ExpressionCondition(expression);
    }

    @RequiredArgsConstructor
    private class ExpressionCondition implements Condition {
        private final String expression;
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            ExpressionContext context = contextFactory.createContext(target, operation);
            context.registerVariable(VAR_TARGET, target);
            context.registerVariable(VAR_OPERATION, operation);
            return Boolean.TRUE.equals(expressionEvaluator.execute(expression, Boolean.class, context));
        }
    }

    /**
     * Context factory for creating expression context
     *
     * @author huangchengxing
     */
    @FunctionalInterface
    public interface ContextFactory {

        /**
         * Create a new expression context
         *
         * @param target the target to be checked
         * @param operation the operation to be checked
         * @return a new expression context
         */
        ExpressionContext createContext(Object target, KeyTriggerOperation operation);
    }
}
