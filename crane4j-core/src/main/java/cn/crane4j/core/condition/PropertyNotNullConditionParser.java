package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnPropertyNotNull;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/**
 * A parser to process {@link ConditionOnPropertyNotNull} annotation.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
public class PropertyNotNullConditionParser
    extends AbstractPropertyConditionParser<ConditionOnPropertyNotNull> {

    public PropertyNotNullConditionParser(
        AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        super(annotationFinder, ConditionOnPropertyNotNull.class, propertyOperator);
    }

    /**
     * Get operation id.
     *
     * @param annotation annotation
     * @return ids
     */
    @Override
    protected String[] getOperationIds(ConditionOnPropertyNotNull annotation) {
        return annotation.id();
    }

    /**
     * Get property name.
     *
     * @param element    element
     * @param annotation annotation
     * @return property name
     */
    @Override
    protected String getPropertyName(
        AnnotatedElement element, ConditionOnPropertyNotNull annotation) {
        return annotation.property();
    }

    /**
     * Check property value.
     *
     * @param propertyValue property value
     * @return boolean
     */
    @Override
    protected boolean checkPropertyValue(Object propertyValue) {
        return Objects.nonNull(propertyValue);
    }
}
