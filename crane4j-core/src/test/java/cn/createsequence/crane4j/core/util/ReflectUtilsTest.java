package cn.createsequence.crane4j.core.util;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

/**
 * test for {@link ReflectUtils}
 *
 * @author huangchengxing
 */
public class ReflectUtilsTest {

    @Test
    public void getDeclaredFields() {
        Field[] fields = ReflectUtils.getDeclaredFields(Foo.class);
        Assert.assertEquals(3, Stream.of(fields).filter(f -> !Modifier.isStatic(f.getModifiers())).count());
        Assert.assertSame(fields, ReflectUtils.getDeclaredFields(Foo.class));
    }

    @Test
    public void findGetterMethodByName() {
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, "standard").isPresent()
        );
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, "fluent").isPresent()
        );
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, "flag").isPresent()
        );
    }

    @SneakyThrows
    @Test
    public void findGetterMethodByField() {
        Field field = Foo.class.getDeclaredField("standard");
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("fluent");
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("flag");
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, field).isPresent()
        );
    }

    @SneakyThrows
    @Test
    public void findSetterMethod() {
        Field field = Foo.class.getDeclaredField("standard");
        Assert.assertTrue(
            ReflectUtils.findSetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("fluent");
        Assert.assertTrue(
            ReflectUtils.findSetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("flag");
        Assert.assertTrue(
            ReflectUtils.findSetterMethod(Foo.class, field).isPresent()
        );
    }

    private static class Foo {

        private Integer standard;

        private Integer fluent;

        private boolean flag;

        public Integer getStandard() {
            return standard;
        }
        public void setStandard(Integer standard) {

        }

        public Integer fluent() {
            return fluent;
        }
        public void fluent(Integer id) {

        }

        public boolean isFlag() {
            return flag;
        }
        public void isFlag(boolean flag) {
        }

    }

}