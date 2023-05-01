package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test for {@link ObjectUtils}
 *
 * @author huangchengxing
 */
public class ObjectUtilsTest {

    @Test
    public void getElementType() {
        // if target is null
        Assert.assertNull(ObjectUtils.getElementType(null));
        // if target is iterator
        Assert.assertEquals(Integer.class, ObjectUtils.getElementType(Arrays.asList(null, null, 3).iterator()));
        // if target is iterable
        Assert.assertEquals(Integer.class, ObjectUtils.getElementType(Arrays.asList(null, null, 3)));
        // if target is array
        Assert.assertEquals(Integer.class, ObjectUtils.getElementType(new Integer[]{null, null, 3}));
        // if target is not collection
        Assert.assertEquals(Integer.class, ObjectUtils.getElementType(1));
    }

    @Test
    public void defaultIfNull() {
        // not mapping
        Assert.assertEquals("default", ObjectUtils.defaultIfNull(null, "default"));
        Assert.assertEquals("not null", ObjectUtils.defaultIfNull("not null", "default"));
    }

    @Test
    public void get() {
        // if target is null
        Assert.assertNull(ObjectUtils.get(null, 0));
        // if target is iterator
        Assert.assertEquals((Integer)3, ObjectUtils.get(Arrays.asList(null, null, 3).iterator(), 2));
        Assert.assertNull(ObjectUtils.get(Arrays.asList(null, null, 3).iterator(), 6));
        // if target is iterable
        Assert.assertEquals((Integer)3, ObjectUtils.get((Iterable<Integer>) () -> Arrays.asList(null, null, 3).iterator(), 2));
        // if target is array
        Assert.assertEquals((Integer)3, ObjectUtils.get(new Integer[]{null, null, 3}, 2));
        Assert.assertNull(ObjectUtils.get(1, 0));
        Assert.assertNull(ObjectUtils.get(Arrays.asList(null, null, 3), -1));
        // if target is map
        Assert.assertEquals((Integer)3, ObjectUtils.get(
            Stream.of(1, 2, 3).collect(Collectors.toMap(Function.identity(), Function.identity())), 2
        ));
    }

    @Test
    public void isEmpty() {
        // if target is null
        Assert.assertTrue(ObjectUtils.isEmpty(null));
        // if target is iterator
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList(null, null, 3).iterator()));
        // if target is iterable
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList(null, null, 3)));
        // if target is array
        Assert.assertFalse(ObjectUtils.isEmpty(new Integer[]{null, null, 3}));
        // if target is not collection
        Assert.assertFalse(ObjectUtils.isEmpty(1));
        // if target is map
        Assert.assertFalse(ObjectUtils.isEmpty(
            Stream.of(1, 2, 3).collect(Collectors.toMap(Function.identity(), Function.identity()))
        ));
        // if target is char sequence
        Assert.assertTrue(ObjectUtils.isEmpty(""));
        Assert.assertFalse(ObjectUtils.isEmpty("not empty"));
    }

    @Test
    public void isNotEmpty() {
        // if target is null
        Assert.assertFalse(ObjectUtils.isNotEmpty(null));
        // if target is iterator
        Assert.assertTrue(ObjectUtils.isNotEmpty(Arrays.asList(null, null, 3).iterator()));
        // if target is iterable
        Assert.assertTrue(ObjectUtils.isNotEmpty(Arrays.asList(null, null, 3)));
        // if target is array
        Assert.assertTrue(ObjectUtils.isNotEmpty(new Integer[]{null, null, 3}));
        // if target is not collection
        Assert.assertTrue(ObjectUtils.isNotEmpty(1));
        // if target is map
        Assert.assertTrue(ObjectUtils.isNotEmpty(
            Stream.of(1, 2, 3).collect(Collectors.toMap(Function.identity(), Function.identity()))
        ));
        // if target is char sequence
        Assert.assertFalse(ObjectUtils.isNotEmpty(""));
        Assert.assertTrue(ObjectUtils.isNotEmpty("not empty"));
    }
}
