package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Lazy}
 *
 * @author huangchengxing
 */
public class LazyTest {

    @Test
    public void get() {
        Lazy<Object> lazy = new Lazy<>(Object::new);
        Object object = lazy.get();
        Assert.assertSame(object, lazy.get());
    }
}