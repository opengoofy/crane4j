package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnTargetType;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * test for {@link ConditionOnTargetTypeParser}
 *
 * @author huangchengxing
 */
public class ConditionOnTargetTypeParserTest {

    @ConditionOnTargetType(value = Member.class)
    private Object field1;
    @ConditionOnTargetType(value = Member.class, strict = true)
    private Object field2;

    @SneakyThrows
    @Test
    public void test() {
        ConditionOnTargetTypeParser parser = new ConditionOnTargetTypeParser(SimpleAnnotationFinder.INSTANCE);

        Field field1 = ConditionOnTargetTypeParserTest.class.getDeclaredField("field1");
        KeyTriggerOperation operation1 = SimpleKeyTriggerOperation.builder()
            .source(field1).id(field1.getName()).key(field1.getName())
            .build();
        Condition condition1 = CollectionUtils.getFirstNotNull(parser.parse(field1, operation1));
        Assert.assertNotNull(condition1);
        Assert.assertTrue(condition1.test(field1, operation1));
        Assert.assertFalse(condition1.test(new Object(), operation1));

        Field field2 = ConditionOnTargetTypeParserTest.class.getDeclaredField("field2");
        KeyTriggerOperation operation2 = SimpleKeyTriggerOperation.builder()
            .source(field2).id(field2.getName()).key(field2.getName())
            .build();
        Condition condition2 = CollectionUtils.getFirstNotNull(parser.parse(field2, operation2));
        Assert.assertNotNull(condition2);
        Assert.assertFalse(condition2.test(field2, operation2));
        Assert.assertFalse(condition2.test(new Object(), operation2));

    }
}
