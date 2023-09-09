package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * test for {@link StringUtils}
 *
 * @author huangchengxing
 */
public class StringUtilsTest {

    @Test
    public void joinForCollection() {
        // is special separator
        Assert.assertEquals("1, 2, 3", StringUtils.join(Arrays.asList(1, 2, 3), String::valueOf, ", "));
        // if null or empty
        Assert.assertEquals("", StringUtils.join(null, String::valueOf, ", "));
        Assert.assertEquals("", StringUtils.join(Collections.emptyList(), String::valueOf, ", "));
    }

    @Test
    public void joinForArray() {
        // is special separator
        Assert.assertEquals("1, 2, 3", StringUtils.join(String::valueOf, ", ", 1, 2, 3));
        // if null or empty
        Assert.assertEquals("", StringUtils.join(String::valueOf, ", "));
    }

    @Test
    public void contains() {
        Assert.assertTrue(StringUtils.contains("abc", "abc"));
        Assert.assertTrue(StringUtils.contains("abc", "b"));
        Assert.assertFalse(StringUtils.contains("abc", "d"));
        Assert.assertFalse(StringUtils.contains(null, "a"));
        Assert.assertFalse(StringUtils.contains("abc", null));
        Assert.assertFalse(StringUtils.contains(null, null));
    }

    @Test
    @SuppressWarnings("all")
    public void format() {
        Assert.assertEquals("abc", StringUtils.format("abc"));
        // if no placeholder
        Assert.assertEquals("abc", StringUtils.format("abc", "b", "c"));
        // if has placeholder but no args
        Assert.assertEquals("abcnull", StringUtils.format("abc{}", (Object)null));
        Assert.assertEquals("abc{}", StringUtils.format("abc{}", new Object[0]));
        // if placeholder is not enough
        Assert.assertEquals("aab", StringUtils.format("a{}{}", "a", "b", "c"));
        Assert.assertEquals("abcd{}", StringUtils.format("abc{}{}", "d"));
    }

    @Test
    public void emptyToDefault() {
        Assert.assertEquals("default", StringUtils.emptyToDefault(null, "default"));
        Assert.assertEquals("default", StringUtils.emptyToDefault("", "default"));
        Assert.assertEquals(" ", StringUtils.emptyToDefault(" ", "default"));
        Assert.assertEquals("a", StringUtils.emptyToDefault("a", "default"));
    }

    @Test
    public void emptyToNull() {
        Assert.assertNull(StringUtils.emptyToNull(null));
        Assert.assertNull(StringUtils.emptyToNull(""));
        Assert.assertEquals(" ", StringUtils.emptyToNull(" "));
        Assert.assertEquals("a", StringUtils.emptyToNull("a"));
    }

    @Test
    public void upperFirst() {
        Assert.assertEquals("Abc", StringUtils.upperFirst("abc"));
        Assert.assertEquals("Abc", StringUtils.upperFirst("Abc"));
        Assert.assertEquals("1bc", StringUtils.upperFirst("1bc"));
        Assert.assertEquals("Abc", StringUtils.upperFirst("abc"));
        // if null or empty
        Assert.assertNull(StringUtils.upperFirst(null));
        Assert.assertEquals("", StringUtils.upperFirst(""));
    }

    @Test
    public void upperFirstAndAddPrefix() {
        Assert.assertEquals("prefixAbc", StringUtils.upperFirstAndAddPrefix("abc", "prefix"));
        Assert.assertEquals("prefixAbc", StringUtils.upperFirstAndAddPrefix("Abc", "prefix"));
        Assert.assertEquals("prefix1bc", StringUtils.upperFirstAndAddPrefix("1bc", "prefix"));
        Assert.assertEquals("prefixAbc", StringUtils.upperFirstAndAddPrefix("abc", "prefix"));
        // if null or empty
        Assert.assertEquals("prefixnull", StringUtils.upperFirstAndAddPrefix(null, "prefix"));
        Assert.assertEquals("prefix", StringUtils.upperFirstAndAddPrefix("", "prefix"));
    }

    @Test
    public void isEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty(" "));
        Assert.assertFalse(StringUtils.isEmpty("a"));
    }

    @Test
    public void isNotEmpty() {
        Assert.assertFalse(StringUtils.isNotEmpty(null));
        Assert.assertFalse(StringUtils.isNotEmpty(""));
        Assert.assertTrue(StringUtils.isNotEmpty(" "));
        Assert.assertTrue(StringUtils.isNotEmpty("a"));
    }

    @Test
    public void isBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank(" "));
        Assert.assertTrue(StringUtils.isBlank(" \t\n\r\f"));
        Assert.assertFalse(StringUtils.isBlank("a"));
    }

    @Test
    public void isNotBlank() {
        Assert.assertFalse(StringUtils.isNotBlank(null));
        Assert.assertFalse(StringUtils.isNotBlank(""));
        Assert.assertFalse(StringUtils.isNotBlank(" "));
        Assert.assertFalse(StringUtils.isNotBlank(" \t\n\r\f"));
        Assert.assertTrue(StringUtils.isNotBlank("a"));
    }
}
