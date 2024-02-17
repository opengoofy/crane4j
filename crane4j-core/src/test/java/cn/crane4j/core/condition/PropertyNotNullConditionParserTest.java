package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnPropertyNotNull;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * test for {@link PropertyNotNullConditionParser}
 *
 * @author huangchengxing
 */
public class PropertyNotNullConditionParserTest {



    @SneakyThrows
    @Test
    public void test() {
        PropertyNotNullConditionParser parser = new PropertyNotNullConditionParser(
            SimpleAnnotationFinder.INSTANCE, ReflectivePropertyOperator.INSTANCE
        );

        Field field = Foo.class.getDeclaredField("field");
        KeyTriggerOperation operation1 = SimpleKeyTriggerOperation.builder()
            .source(field).id(field.getName()).key(field.getName())
            .build();
        Collection<Condition> conditions = parser.parse((AnnotatedElement)operation1.getSource(), operation1).get(operation1.getId());
        Assert.assertEquals(1, conditions.size());
        Condition condition = CollectionUtils.getFirstNotNull(conditions);
        Assert.assertNotNull(condition);
        Assert.assertTrue(condition.test(new Foo("success"), operation1));
        Assert.assertFalse(condition.test(new Foo(null), operation1));
    }

    @Getter
    @AllArgsConstructor
    private static class Foo {
        @ConditionOnPropertyNotNull
        private String field;
    }
}
