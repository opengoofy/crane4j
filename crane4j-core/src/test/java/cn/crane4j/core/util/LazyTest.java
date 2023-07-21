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
        Assert.assertFalse(lazy.isInitialized());
        Object object = lazy.get();
        Assert.assertSame(object, lazy.get());
        Assert.assertTrue(lazy.isInitialized());
    }

    @Test
    public void initAsNull() {
        Lazy<Object> lazy = new Lazy<>(() -> null);
        Assert.assertFalse(lazy.isInitialized());
        Object object = lazy.get();
        Assert.assertNull(object);
        Assert.assertTrue(lazy.isInitialized());
        Assert.assertNull(lazy.get());
        Assert.assertTrue(lazy.isInitialized());
    }

    @Test
    public void refresh() {
        Lazy<Object> lazy = new Lazy<>(Object::new);
        Object object = lazy.get();
        Assert.assertTrue(lazy.isInitialized());
        lazy.refresh();
        Assert.assertFalse(lazy.isInitialized());
        Assert.assertNotSame(object, lazy.get());
    }
}