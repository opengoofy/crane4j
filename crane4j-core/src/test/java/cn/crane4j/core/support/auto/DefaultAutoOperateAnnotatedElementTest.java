package cn.crane4j.core.support.auto;

import cn.crane4j.core.parser.BeanOperations;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link DefaultAutoOperateAnnotatedElement }
 *
 * @author huangchengxing
 */
public class DefaultAutoOperateAnnotatedElementTest {

    @Test
    public void test() {
        DefaultAutoOperateAnnotatedElement element = new DefaultAutoOperateAnnotatedElement(
            null, null, (t, args) -> t, null, null, (targets, operations, filter) -> {}, t -> BeanOperations.empty()
        );
        Assert.assertNull(element.getAnnotation());
        Assert.assertNull(element.getElement());
        Assert.assertNull(element.getBeanOperations());
        element.execute(null);
        element.execute(new Object());
    }
}
