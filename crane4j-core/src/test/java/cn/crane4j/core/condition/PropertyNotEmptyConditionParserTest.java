package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnPropertyNotEmpty;
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
import java.util.Collections;
import java.util.List;

/**
 * test for {@link PropertyNotEmptyConditionParser}
 *
 * @author huangchengxing
 */
public class PropertyNotEmptyConditionParserTest {

    @SneakyThrows
    @Test
    public void test() {
        PropertyNotEmptyConditionParser parser = new PropertyNotEmptyConditionParser(
            SimpleAnnotationFinder.INSTANCE, ReflectivePropertyOperator.INSTANCE
        );

        Field field1 = Foo.class.getDeclaredField("field1");
        KeyTriggerOperation operation1 = SimpleKeyTriggerOperation.builder()
            .source(field1).id(field1.getName()).key(field1.getName())
            .build();
        Collection<Condition> conditions1 = parser.parse((AnnotatedElement)operation1.getSource(), operation1).get(operation1.getId());
        Assert.assertEquals(1, conditions1.size());
        Condition condition1 = CollectionUtils.getFirstNotNull(conditions1);
        Assert.assertNotNull(condition1);
        Assert.assertTrue(condition1.test(new Foo("success", null), operation1));
        Assert.assertFalse(condition1.test(new Foo("", null), operation1));
        Assert.assertFalse(condition1.test(new Foo(null, null), operation1));

        Field field2 = Foo.class.getDeclaredField("field2");
        KeyTriggerOperation operation2 = SimpleKeyTriggerOperation.builder()
            .source(field2).id(field2.getName()).key(field2.getName())
            .build();
        Collection<Condition> conditions2 = parser.parse((AnnotatedElement)operation2.getSource(), operation2).get(operation2.getId());
        Assert.assertEquals(1, conditions2.size());
        Condition condition2 = CollectionUtils.getFirstNotNull(conditions2);
        Assert.assertNotNull(condition2);
        Assert.assertTrue(condition2.test(new Foo(null, Collections.singletonList("success")), operation1));
        Assert.assertFalse(condition2.test(new Foo(null, Collections.emptyList()), operation1));
        Assert.assertFalse(condition2.test(new Foo(null, null), operation1));
    }

    @Getter
    @AllArgsConstructor
    private static class Foo {
        @ConditionOnPropertyNotEmpty
        private String field1;
        @ConditionOnPropertyNotEmpty(property = "field2")
        private List<Object> field2;
    }
}
