package cn.crane4j.core.support.auto;

import cn.crane4j.core.parser.BeanOperations;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link AutoOperateAnnotatedElement}
 *
 * @author huangchengxing
 */
public class AutoOperateAnnotatedElementTest {

    @Test
    public void test() {
        AutoOperateAnnotatedElement element = AutoOperateAnnotatedElement.EMPTY;
        element.execute(null);
        element.execute(new Object());
        Assert.assertNull(element.getAnnotation());
        Assert.assertSame(BeanOperations.empty(), element.getBeanOperations());
        Assert.assertNull(element.getElement());
    }
}
