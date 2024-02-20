package cn.crane4j.core.condition;

import cn.crane4j.annotation.condition.ConditionOnContainer;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.DefaultContainerManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * test for {@link ConditionOnContainerParser}
 *
 * @author huangchengxing
 */
public class ConditionOnContainerParserTest {

    @ConditionOnContainer(value = "present")
    private Object field1;
    @ConditionOnContainer(value = "absent")
    private Object field2;

    @SneakyThrows
    @Test
    public void test() {
        ContainerManager manager = new DefaultContainerManager();
        manager.registerContainer(Containers.forEmptyData("present"));
        ConditionOnContainerParser parser = new ConditionOnContainerParser(SimpleAnnotationFinder.INSTANCE, manager);

        Field field1 = ConditionOnContainerParserTest.class.getDeclaredField("field1");
        KeyTriggerOperation operation1 = SimpleKeyTriggerOperation.builder()
            .source(field1).id(field1.getName()).key(field1.getName())
            .build();
        Condition condition1 = CollectionUtils.getFirstNotNull(parser.parse(field1, operation1));
        Assert.assertNotNull(condition1);
        Assert.assertTrue(condition1.test(field1, operation1));

        Field field2 = ConditionOnContainerParserTest.class.getDeclaredField("field2");
        KeyTriggerOperation operation2 = SimpleKeyTriggerOperation.builder()
            .source(field2).id(field2.getName()).key(field2.getName())
            .build();
        Condition condition2 = CollectionUtils.getFirstNotNull(parser.parse(field2, operation2));
        Assert.assertNotNull(condition2);
        Assert.assertFalse(condition2.test(field2, operation2));
    }

}
