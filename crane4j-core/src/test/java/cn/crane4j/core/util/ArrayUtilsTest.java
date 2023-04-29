package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ArrayUtils}
 *
 * @author huangchengxing
 */
public class ArrayUtilsTest {

    @Test
    public void append() {
        String[] array = {"a", "b", "c"};
        String[] append = ArrayUtils.append(array, "d");
        // is not the same array
        Assert.assertNotSame(array, append);
        Assert.assertEquals(4, append.length);
        Assert.assertEquals("d", append[3]);
        Assert.assertEquals("c", append[2]);
        Assert.assertEquals("b", append[1]);
        Assert.assertEquals("a", append[0]);
        // if array is null, return elements directly
        String[] elements = {"d"};
        Assert.assertSame(elements, ArrayUtils.append(null, elements));
        // if append is null
        Assert.assertEquals(4, ArrayUtils.append(array, (String)null).length);
        // if append is empty
        Assert.assertEquals(3, ArrayUtils.append(array, new String[]{}).length);
    }

    @Test
    public void isEmpty() {
        Assert.assertTrue(ArrayUtils.isEmpty(null));
        Assert.assertTrue(ArrayUtils.isEmpty(new String[]{}));
        Assert.assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));
    }

    @Test
    public void join() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals("a,b,c", ArrayUtils.join(array, ","));
        // if array is null, return empty string
        Assert.assertEquals("", ArrayUtils.join(null, ","));
    }

    @Test
    public void joinForStringArray() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals("a,b,c", ArrayUtils.join(array, ","));
        // if array is null, return empty string
        Assert.assertEquals("", ArrayUtils.join(null, ","));
    }

    @Test
    public void contains() {
        String[] array = {"a", "b", "c"};
        Assert.assertTrue(ArrayUtils.contains(array, "a"));
        Assert.assertFalse(ArrayUtils.contains(array, "d"));
        Assert.assertFalse(ArrayUtils.contains(null, "a"));
        Assert.assertFalse(ArrayUtils.contains(null, null));
    }

    @Test
    public void stream() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals(3, ArrayUtils.stream(array).count());
        Assert.assertEquals(0, ArrayUtils.stream(null).count());
    }

    @Test
    public void get() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals("a", ArrayUtils.get(array, 0));
        Assert.assertNull(ArrayUtils.get(array, -1));
        Assert.assertEquals("c", ArrayUtils.get(array, 2));
        Assert.assertNull(ArrayUtils.get(array, 3));
        Assert.assertNull(ArrayUtils.get(array, -4));
        Assert.assertNull(ArrayUtils.get(null, 0));
    }

    @Test
    public void length() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals(3, ArrayUtils.length(array));
        Assert.assertEquals(0, ArrayUtils.length(null));
    }

    @Test
    public void isEqualsForDefault() {
        String[] array1 = {"a", "b", "c"};
        String[] array2 = {"a", "b", "c"};
        String[] array3 = {"a", "b", "d"};
        Assert.assertTrue(ArrayUtils.isEquals(array1, array2));
        Assert.assertFalse(ArrayUtils.isEquals(array1, array3));
        Assert.assertFalse(ArrayUtils.isEquals(array1, null));
        Assert.assertFalse(ArrayUtils.isEquals(null, array2));
        Assert.assertTrue(ArrayUtils.isEquals(null, null));
        // length not equals
        String[] array4 = {"a", "b"};
        Assert.assertFalse(ArrayUtils.isEquals(array1, array4));
    }

    @Test
    public void isEqualsForPredicate() {
        String[] array1 = {"a", "b", "c"};
        String[] array2 = {"a", "b", "c"};
        String[] array3 = {"a", "b", "d"};
        Assert.assertTrue(ArrayUtils.isEquals(array1, array2, String::equals));
        Assert.assertFalse(ArrayUtils.isEquals(array1, array3, String::equals));
        Assert.assertFalse(ArrayUtils.isEquals(array1, null, String::equals));
        Assert.assertFalse(ArrayUtils.isEquals(null, array2, String::equals));
        Assert.assertTrue(ArrayUtils.isEquals(null, null, String::equals));
        // length not equals
        String[] array4 = {"a", "b"};
        Assert.assertFalse(ArrayUtils.isEquals(array1, array4, String::equals));
        // if predicate is null, throw NullPointerException
        Assert.assertThrows(NullPointerException.class, () -> ArrayUtils.isEquals(array1, array2, null));
        // if predicate is null, but not use predicate
        Assert.assertTrue(ArrayUtils.isEquals(array1, array1, null));
    }

}
