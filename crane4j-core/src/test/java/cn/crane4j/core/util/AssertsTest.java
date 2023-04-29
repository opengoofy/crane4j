package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link Asserts}
 *
 * @author huangchengxing
 */
public class AssertsTest {

    @Test
    public void isNotEquals() {
        Object obj = new Object();
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEquals(obj, obj, () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEquals(obj, obj, "test"));
    }

    @Test
    public void isEquals() {
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEquals(new Object(), new Object(), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEquals(new Object(), new Object(), "test"));
    }

    @Test
    public void isTrue() {
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isTrue(false, () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isTrue(false, "test"));
    }

    @Test
    public void isFalse() {
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isFalse(true, () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isFalse(true, "test"));
    }

    @Test
    public void isNull() {
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNull(new Object(), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNull(new Object(), "test"));
    }

    @Test
    public void notNull() {
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotNull(null, () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotNull(null, "test"));
    }

    @Test
    public void isEmpty() {
        // object
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(new Object(), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(new Object(), "test"));
        // array
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(new Object[1], () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(new Object[1], "test"));
        // collection
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(Collections.singletonList(1), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(Collections.singletonList(1), "test"));
        // map
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(Collections.singletonMap(1, 1), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty(Collections.singletonMap(1, 1), "test"));
        // string
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty("test", () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isEmpty("test", "test"));
    }

    @Test
    public void isNotEmpty() {
        // object
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(null, () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(null, "test"));
        // array
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(new Object[0], () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(new Object[0], "test"));
        // collection
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(Lists.newArrayList(), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(Lists.newArrayList(), "test"));
        // map
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(Collections.emptyMap(), () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty(Collections.emptyMap(), "test"));
        // string
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty("", () -> new Crane4jException("test")));
        Assert.assertThrows(Crane4jException.class, () -> Asserts.isNotEmpty("", "test"));
    }
}
