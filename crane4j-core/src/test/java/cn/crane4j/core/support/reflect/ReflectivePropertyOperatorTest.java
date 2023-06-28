package cn.crane4j.core.support.reflect;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ReflectivePropertyOperator}
 *
 * @author huangchengxing
 */
public class ReflectivePropertyOperatorTest {

    private final ReflectivePropertyOperator operator = new ReflectivePropertyOperator(new HutoolConverterManager());

    @Test
    public void readProperty() {
        Foo foo = new Foo(12, true);
        operator.readProperty(Foo.class, foo, "noneField");
        Assert.assertEquals(12, operator.readProperty(Foo.class, foo, "id"));
        Assert.assertEquals(true, operator.readProperty(Foo.class, foo, "flag"));
    }

    @Test
    public void findGetter() {
        MethodInvoker getter = operator.findGetter(Foo.class, "id");
        Assert.assertNotNull(getter);
        Assert.assertNull(operator.findGetter(Foo.class, "none"));

        operator.setThrowIfNoMatchedMethod(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findGetter(Foo.class, "none"));
        operator.setThrowIfNoMatchedMethod(false);
    }

    @Test
    public void writeProperty() {
        Foo foo = new Foo(1, true);
        operator.writeProperty(Foo.class, foo, "noneField", null);
        operator.writeProperty(Foo.class, foo, "id", 2);
        Assert.assertEquals((Integer)2, foo.getId());
        operator.writeProperty(Foo.class, foo, "flag", false);
        Assert.assertFalse(foo.isFlag());
    }

    @Test
    public void findSetter() {
        operator.setConverterManager(null);

        MethodInvoker setter = operator.findSetter(Foo.class, "id");
        Assert.assertNotNull(setter);
        Assert.assertNull(operator.findSetter(Foo.class, "none"));

        operator.setThrowIfNoMatchedMethod(true);
        Assert.assertThrows(Crane4jException.class, () -> operator.findSetter(Foo.class, "none"));
        operator.setThrowIfNoMatchedMethod(false);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Foo {
        private Integer id;
        private boolean flag;
    }

}
