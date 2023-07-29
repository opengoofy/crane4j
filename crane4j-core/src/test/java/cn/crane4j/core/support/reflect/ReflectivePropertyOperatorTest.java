package cn.crane4j.core.support.reflect;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * test for {@link ReflectivePropertyOperator}
 *
 * @author huangchengxing
 */
public class ReflectivePropertyOperatorTest {

    private final ReflectivePropertyOperator operator = new ReflectivePropertyOperator(new HutoolConverterManager());

    @Test
    public void readProperty() {
        Foo foo = new Foo(12, true, "intact", "shade");
        Foo.shared = "shared";
        Assert.assertNull(operator.readProperty(Foo.class, foo, "noneField"));
        Assert.assertEquals(12, operator.readProperty(Foo.class, foo, "id"));
        Assert.assertEquals(true, operator.readProperty(Foo.class, foo, "flag"));
        Assert.assertEquals("intact", operator.readProperty(Foo.class, foo, "intact"));
        Assert.assertEquals("shade", operator.readProperty(Foo.class, foo, "shade"));
        Assert.assertEquals("shared", operator.readProperty(Foo.class, foo, "shared"));
    }

    @Test
    public void findGetter() {
        Assert.assertNotNull(operator.findGetter(Foo.class, "id"));
        Assert.assertNull(operator.findGetter(Foo.class, "none"));
        Assert.assertNotNull(operator.findGetter(Foo.class, "shared"));

        operator.setThrowIfNoMatchedMethod(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findGetter(Foo.class, "none"));
        operator.setThrowIfNoMatchedMethod(false);
    }

    @Test
    public void writeProperty() {
        Foo foo = new Foo(1, true, "intact", "shade");
        operator.writeProperty(Foo.class, foo, "noneField", null);
        operator.writeProperty(Foo.class, foo, "id", 2);
        Assert.assertEquals((Integer)2, foo.getId());
        operator.writeProperty(Foo.class, foo, "flag", false);
        Assert.assertFalse(foo.flag);
        operator.writeProperty(Foo.class, foo, "intact", "other");
        Assert.assertEquals("other", foo.getIntact());
        operator.writeProperty(Foo.class, foo, "shade", "another");
        Assert.assertEquals("another", foo.getShade());
        operator.writeProperty(Foo.class, foo, "shared", "changed");
        Assert.assertEquals("changed", Foo.shared);
    }

    @Test
    public void findSetter() {
        operator.setConverterManager(null);

        Assert.assertNotNull(operator.findSetter(Foo.class, "id"));
        Assert.assertNotNull(operator.findSetter(Foo.class, "flag"));
        Assert.assertNotNull(operator.findSetter(Foo.class, "intact"));
        Assert.assertNotNull(operator.findSetter(Foo.class, "shade"));
        Assert.assertNotNull(operator.findSetter(Foo.class, "shared"));
        Assert.assertNull(operator.findSetter(Foo.class, "none"));

        operator.setThrowIfNoMatchedMethod(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findSetter(Foo.class, "none"));
        operator.setThrowIfNoMatchedMethod(false);
    }

    @Test
    public void testCustomReflectivePropertyOperator() {
        CustomReflectivePropertyOperator operator = new CustomReflectivePropertyOperator();
        Assert.assertNotNull(operator.findSetter(Foo.class, "id"));
        Assert.assertNull(operator.findSetter(Foo.class, "flag"));
        Assert.assertNull(operator.findSetter(Foo.class, "shade"));
        Assert.assertNull(operator.findSetter(Foo.class, "none"));

        Foo foo = new Foo(1, true, "intact", "shade");
        operator.writeProperty(Foo.class, foo, "id", 2);
        Assert.assertEquals((Integer)2, foo.getId());
        operator.writeProperty(Foo.class, foo, "flag", false);
        Assert.assertTrue(foo.flag);
        operator.writeProperty(Foo.class, foo, "intact", "other");
        Assert.assertEquals("other", foo.getIntact());
        operator.writeProperty(Foo.class, foo, "shade", "another");
        Assert.assertEquals("shade", foo.getShade());

        operator.setThrowIfNoMatchedMethod(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findSetter(Foo.class, "none"));
        operator.setThrowIfNoMatchedMethod(false);
    }

    @AllArgsConstructor
    private static class Foo {

        @Getter
        private Integer id;

        @Setter
        private boolean flag;

        @Setter
        @Getter
        private String intact;

        private String _shade;

        public static String shared;

        public String getShade() {
            return _shade;
        }

        public void setShade(String shade) {
            _shade = shade;
        }
    }

    private static class CustomReflectivePropertyOperator extends ReflectivePropertyOperator {

        private static final List<String> immutableFields = Arrays.asList("flag", "shade");

        public CustomReflectivePropertyOperator() {
            super(new HutoolConverterManager());
        }

        @Override
        protected @Nullable MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method) {
            if (immutableFields.contains(propertyName)) {
                return null;
            }
            return super.createInvoker(targetType, propertyName, method);
        }

        @Override
        protected MethodInvoker createInvokerForSetter(Class<?> targetType, String propertyName, Field field) {
            if (immutableFields.contains(propertyName)) {
                return null;
            }
            return super.createInvokerForSetter(targetType, propertyName, field);
        }
    }

}
