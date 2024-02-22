package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionType;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

/**
 * test for {@link AbstractConditionParser}
 *
 * @author huangchengxing
 */
public class AbstractConditionParserTest {

    @TestConditionAnnotation2(id = "field1")
    @TestConditionAnnotation1
    private Field field1;

    // ============= not properties annotation =============

    @SneakyThrows
    @Test
    public void test1() {
        TestConditionAnnotationParser1 parser = new TestConditionAnnotationParser1(SimpleAnnotationFinder.INSTANCE);
        Field field1 = AbstractConditionParserTest.class.getDeclaredField("field1");
        Assert.assertNotNull(field1);
        KeyTriggerOperation operation1 = SimpleKeyTriggerOperation.builder()
            .source(field1).id(field1.getName()).key(field1.getName())
            .build();
        Condition condition1 = CollectionUtils.getFirstNotNull(parser.parse(field1, operation1));
        Assert.assertNotNull(condition1);
        Assert.assertTrue(condition1.test(null, operation1));
    }

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestConditionAnnotation1 {
    }

    private static class TestConditionAnnotationParser1
        extends AbstractConditionParser<TestConditionAnnotation1> {
        public TestConditionAnnotationParser1(AnnotationFinder annotationFinder) {
            super(annotationFinder, TestConditionAnnotation1.class);
        }
        @Nullable
        @Override
        protected AbstractCondition createCondition(
            AnnotatedElement element, TestConditionAnnotation1 annotation) {
            return new AbstractCondition() {
                @Override
                public boolean test(Object target, KeyTriggerOperation operation) {
                    return true;
                }
            };
        }
    }

    // ============= standard annotation =============

    @SneakyThrows
    @Test
    public void test2() {
        TestConditionAnnotationParser2 parser = new TestConditionAnnotationParser2(SimpleAnnotationFinder.INSTANCE);
        Field field1 = AbstractConditionParserTest.class.getDeclaredField("field1");
        Assert.assertNotNull(field1);
        KeyTriggerOperation operation1 = SimpleKeyTriggerOperation.builder()
            .source(field1).id(field1.getName()).key(field1.getName())
            .build();
        Condition condition1 = CollectionUtils.getFirstNotNull(parser.parse(field1, operation1));
        Assert.assertNotNull(condition1);
        Assert.assertTrue(condition1.test(null, operation1));


        KeyTriggerOperation operation2 = SimpleKeyTriggerOperation.builder()
            .source(field1).id("none").key(field1.getName())
            .build();
        Assert.assertTrue(parser.parse(field1, operation2).isEmpty());
    }

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestConditionAnnotation2 {
        String[] id() default {};
        ConditionType type() default ConditionType.AND;
        boolean negate() default false;
        int sort() default Integer.MAX_VALUE;
    }

    private static class TestConditionAnnotationParser2
        extends AbstractConditionParser<TestConditionAnnotation2> {
        public TestConditionAnnotationParser2(AnnotationFinder annotationFinder) {
            super(annotationFinder, TestConditionAnnotation2.class);
        }
        @NonNull
        @Override
        protected ConditionDescriptor getConditionDescriptor(TestConditionAnnotation2 annotation) {
            return ConditionDescriptor.builder()
                .boundOperationIds(annotation.id()) // 条件要绑定到哪些操作上
                .type(annotation.type()) // 当有多个条件时，该条件应该是 AND 还是 OR
                .sort(annotation.sort()) // 当有多个条件时，该条件应该排在第几个
                .negate(annotation.negate()) // 该条件是否需要取反
                .build();
        }
        @Nullable
        @Override
        protected AbstractCondition createCondition(
            AnnotatedElement element, TestConditionAnnotation2 annotation) {
            return new AbstractCondition() {
                @Override
                public boolean test(Object target, KeyTriggerOperation operation) {
                    return true;
                }
            };
        }
    }
}
