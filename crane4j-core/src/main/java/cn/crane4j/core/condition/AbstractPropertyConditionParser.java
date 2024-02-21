package cn.crane4j.core.condition;

import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * A condition parser implementation to process annotation based condition with property.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
public abstract class AbstractPropertyConditionParser<A extends Annotation> extends AbstractConditionParser<A> {

    private final PropertyOperator propertyOperator;

    protected AbstractPropertyConditionParser(
        AnnotationFinder annotationFinder, Class<A> annotationType,
        PropertyOperator propertyOperator) {
        super(annotationFinder, annotationType);
        this.propertyOperator = propertyOperator;
    }

    /**
     * Create condition instance.
     *
     * @param element element
     * @param annotation annotation
     * @return condition instance
     */
    @Nullable
    @Override
    protected AbstractCondition createCondition(AnnotatedElement element, A annotation) {
        String property = getPropertyName(element, annotation);
        property = ConfigurationUtil.getElementIdentifier(element, property);
        Asserts.isNotEmpty(property, "The property to be checked is not specified in the @{} on {}", annotationType.getSimpleName());
        return new PropertyValueCondition(property);
    }

    /**
     * Get property name.
     *
     * @param element element
     * @param annotation annotation
     * @return property name
     */
    protected abstract String getPropertyName(AnnotatedElement element, A annotation);

    /**
     * Check property value.
     *
     * @param propertyValue property value
     * @return boolean
     */
    protected abstract boolean checkPropertyValue(Object propertyValue);

    @RequiredArgsConstructor
    private class PropertyValueCondition extends AbstractCondition {
        private final String property;
        @Override
        public boolean test(Object target, KeyTriggerOperation operation) {
            Object value = propertyOperator.readProperty(target.getClass(), target, property);
            return checkPropertyValue(value);
        }
    }
}
