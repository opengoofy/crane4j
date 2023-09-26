package cn.crane4j.core.support.aop;

import cn.crane4j.core.parser.BeanOperations;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link AutoOperateAnnotatedMethod }
 *
 * @author huangchengxing
 */
public class AutoOperateAnnotatedMethodTest {

    @Test
    public void test() {
        AutoOperateAnnotatedMethod element = new AutoOperateAnnotatedMethod(
            null, null, (t, args) -> t, null, null, (targets, operations, filter) -> {}, t -> BeanOperations.empty()
        );
        Assert.assertNull(element.getAnnotation());
        Assert.assertNull(element.getElement());
        Assert.assertNull(element.getBeanOperations());
        element.execute(null);
        element.execute(new Object());
    }
}
