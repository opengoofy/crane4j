package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnExpression;
import cn.crane4j.annotation.condition.ConditionOnProperty;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A parser to process {@link ConditionOnProperty} annotation.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
public class PropertyConditionParser
    extends AbstractConditionParser<ConditionOnProperty> {

    private final PropertyOperator propertyOperator;
    private final ConverterManager converterManager;

    public PropertyConditionParser(
        AnnotationFinder annotationFinder,
        PropertyOperator propertyOperator, ConverterManager converterManager) {
        super(annotationFinder, ConditionOnProperty.class);
        this.propertyOperator = propertyOperator;
        this.converterManager = converterManager;
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
    protected AbstractCondition createCondition(AnnotatedElement element, ConditionOnProperty annotation) {
        String property = getPropertyName(element, annotation);
        boolean convertTypeWhenTest = ClassUtils.isObjectOrVoid(annotation.valueType());
        Object expectedValue = convertTypeWhenTest ?
            annotation.value() : converterManager.convert(annotation.value(), annotation.valueType());
        return new PropertyEqualsCondition(property, expectedValue, convertTypeWhenTest, annotation.enableNull());
    }

    /**
     * Get condition properties.
     *
     * @param annotation annotation
     * @return condition properties
     */
    @NonNull
    @Override
    protected ConditionDescriptor getConditionDescriptor(ConditionOnProperty annotation) {
        return ConditionDescriptor.builder()
            .operationIds(annotation.id())
            .type(annotation.type())
            .sort(annotation.sort())
            .negate(annotation.negation())
            .build();
    }

    private String getPropertyName(
        AnnotatedElement element, ConditionOnProperty annotation) {
        String property = annotation.property();
        property = StringUtils.isEmpty(property) && element instanceof Field ?
            ((Field)element).getName() : property;
        Asserts.isNotEmpty(property, "@{} must specify a property to apply!", ConditionOnExpression.class.getSimpleName());
        return property;
    }

    /**
     * Check whether the expected value equals to the actual value.
     *
     * @param expected expected
     * @param actual actual
     * @return true if equals, otherwise false
     */
    protected boolean check(Object expected, Object actual) {
        return Objects.equals(expected, actual);
    }

    @RequiredArgsConstructor
    private class PropertyEqualsCondition extends AbstractCondition {
        private final String property;
        private final Object expectedValue;
        private final boolean convertTypeWhenTest;
        private final boolean enableNull;
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            Object actual = propertyOperator.readProperty(target.getClass(), target, property);
            if (Objects.isNull(actual)) {
                return enableNull || Objects.isNull(expectedValue);
            }
            return convertTypeWhenTest ?
                check(converterManager.convert(expectedValue, actual.getClass()), actual) :
                check(expectedValue, actual);
        }
    }
}
