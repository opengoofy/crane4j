package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnExpression;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * test for {@link ConditionOnExpressionParser}
 *
 * @author huangchengxing
 */
public class ConditionOnExpressionParserTest {

    @ConditionOnExpression(value = "#target.status == 'success'")
    private Object annotated;
    private Object notAnnotated;

    @SneakyThrows
    @Test
    public void test() {
        ConditionOnExpressionParser parser = new ConditionOnExpressionParser(
            SimpleAnnotationFinder.INSTANCE, new OgnlExpressionEvaluator(), (t, op) -> new OgnlExpressionContext()
        );

        Field annotatedField = ConditionOnExpressionParserTest.class.getDeclaredField("annotated");
        KeyTriggerOperation operationOfAnnotatedField = SimpleKeyTriggerOperation.builder()
            .source(annotatedField)
            .id(annotatedField.getName())
            .key(annotatedField.getName())
            .build();
        Collection<Condition> conditions = parser.parse((AnnotatedElement)operationOfAnnotatedField.getSource(), operationOfAnnotatedField);
        Assert.assertEquals(1, conditions.size());
        Condition condition = CollectionUtils.getFirstNotNull(conditions);
        Assert.assertNotNull(condition);
        Assert.assertTrue(condition.test(new Foo("success"), operationOfAnnotatedField));

        Field notAnnotatedField = ConditionOnExpressionParserTest.class.getDeclaredField("notAnnotated");
        KeyTriggerOperation operationOfNotAnnotatedField = SimpleKeyTriggerOperation.builder()
            .source(notAnnotatedField)
            .id(notAnnotatedField.getName())
            .key(notAnnotatedField.getName())
            .build();
        Assert.assertTrue(parser.parse((AnnotatedElement)operationOfNotAnnotatedField.getSource(), operationOfNotAnnotatedField).isEmpty());
        Assert.assertTrue(parser.parse((AnnotatedElement)operationOfNotAnnotatedField.getSource(), operationOfNotAnnotatedField).isEmpty());
    }

    @Getter
    @RequiredArgsConstructor
    private static class Foo {
        private final String status;
    }
}
