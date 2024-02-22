package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnPropertyNotNull;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/**
 * A parser to process {@link ConditionOnPropertyNotNull} annotation.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
public class ConditionOnPropertyNotNullParser
    extends AbstractPropertyConditionParser<ConditionOnPropertyNotNull> {

    public ConditionOnPropertyNotNullParser(
        AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        super(annotationFinder, ConditionOnPropertyNotNull.class, propertyOperator);
    }

    /**
     * Get condition properties.
     *
     * @param annotation annotation
     * @return condition properties
     */
    @NonNull
    @Override
    protected ConditionDescriptor getConditionDescriptor(ConditionOnPropertyNotNull annotation) {
        return ConditionDescriptor.builder()
            .boundOperationIds(annotation.id())
            .type(annotation.type())
            .sort(annotation.sort())
            .negate(annotation.negate())
            .build();
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
