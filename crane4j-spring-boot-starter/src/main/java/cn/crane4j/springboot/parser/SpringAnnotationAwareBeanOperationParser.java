package cn.crane4j.springboot.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.ReflectUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * <p>Extension implementation of {@link SpringAnnotationAwareBeanOperationParser},
 * On the basis of the former, some spring annotations are additionally supported.
 *
 * @author huangchengxing
 * @see Order
 */
public class SpringAnnotationAwareBeanOperationParser extends AnnotationAwareBeanOperationParser {

    /**
     * <p>Create an operation parser that supports annotation configuration.<br />
     * The order of operation configurations is {@link Order#value()} or {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration global configuration
     */
    public SpringAnnotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        super(annotationFinder, globalConfiguration, Sorted.comparator());
    }

    /**
     * Parse {@link Assemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Assemble}
     */
    @Override
    protected List<Assemble> parseAssembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(
            beanType, Assemble.class, SpringAnnotationAwareBeanOperationParser::processAnnotation
        );
    }

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     */
    @Override
    protected List<Disassemble> parseDisassembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(
            beanType, Disassemble.class, SpringAnnotationAwareBeanOperationParser::processAnnotation
        );
    }

    private static <T extends Annotation> T processAnnotation(T a, Field f) {
        // force value to be set to the annotated attribute name
        ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
        // force sort if field annotated by @Order
        Order annotation = AnnotatedElementUtils.findMergedAnnotation(f, Order.class);
        if (Objects.nonNull(annotation)) {
            ReflectUtils.setAttributeValue(a, "sort", annotation.value());
        }
        return a;
    }
}
