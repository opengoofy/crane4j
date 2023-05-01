package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;

/**
 * test for {@link ClassUtils}
 *
 * @author huangchengxing
 */
public class ClassUtilsTest {

    @Test
    public void isJdkClass() {
        Assert.assertTrue(ClassUtils.isJdkClass(String.class));
        Assert.assertTrue(ClassUtils.isJdkClass(Nullable.class));
        Assert.assertFalse(ClassUtils.isJdkClass(ClassUtilsTest.class));
        Assert.assertThrows(NullPointerException.class, () -> ClassUtils.isJdkClass(null));
    }

    @Test
    public void forName() {
        Assert.assertEquals(String.class, ClassUtils.forName("java.lang.String"));
        Assert.assertEquals(ClassUtilsTest.class, ClassUtils.forName("cn.crane4j.core.util.ClassUtilsTest"));
        Assert.assertThrows(Crane4jException.class, () -> ClassUtils.forName("not.found.class"));
        Assert.assertThrows(NullPointerException.class, () -> ClassUtils.forName(null));
    }

    @Test
    public void packageToPath() {
        Assert.assertEquals("cn/crane4j/core/util/ClassUtils", ClassUtils.packageToPath("cn.crane4j.core.util.ClassUtils"));
        Assert.assertThrows(NullPointerException.class, () -> ClassUtils.packageToPath(null));
    }
}
