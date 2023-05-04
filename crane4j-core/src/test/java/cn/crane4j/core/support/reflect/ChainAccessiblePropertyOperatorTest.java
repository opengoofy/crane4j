package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link ChainAccessiblePropertyOperator}.
 *
 * @author huangchengxing
 */
public class ChainAccessiblePropertyOperatorTest {

    private ChainAccessiblePropertyOperator operator;

    @Before
    public void init() {
        operator = new ChainAccessiblePropertyOperator(new ReflectPropertyOperator(new HutoolConverterManager()));
    }

    @Test
    public void readProperty() {
        Foo foo = new Foo(1, new Foo(2, new Foo(3, new Foo(4, null))));
        Assert.assertNull(operator.readProperty(Foo.class, foo, "foo.foo.foo.foo.id"));
        Assert.assertEquals(4, operator.readProperty(Foo.class, foo, "foo.foo.foo.id"));
        Assert.assertEquals(3, operator.readProperty(Foo.class, foo, "foo.foo.id"));
        Assert.assertEquals(2, operator.readProperty(Foo.class, foo, "foo.id"));
        Assert.assertEquals(1, operator.readProperty(Foo.class, foo, "id"));
    }

    @Test
    public void findGetter() {
        Foo foo = new Foo(1, new Foo(2, new Foo(3, new Foo(4, null))));

        MethodInvoker getter = operator.findGetter(Foo.class, "foo.foo.foo.id");
        Assert.assertNotNull(getter);
        Assert.assertEquals(4, getter.invoke(foo));

        getter = operator.findGetter(Foo.class, "foo.foo.id");
        Assert.assertNotNull(getter);
        Assert.assertEquals(3, getter.invoke(foo));

        getter = operator.findGetter(Foo.class, "foo.id");
        Assert.assertNotNull(getter);
        Assert.assertEquals(2, getter.invoke(foo));

        getter = operator.findGetter(Foo.class, "id");
        Assert.assertNotNull(getter);
        Assert.assertEquals(1, getter.invoke(foo));
    }

    @Test
    public void writeProperty() {
        Foo foo = new Foo(null, new Foo(null, new Foo(null, new Foo(null, null))));

        operator.writeProperty(Foo.class, foo, "id", 1);
        Assert.assertEquals((Integer)1, foo.getId());

        operator.writeProperty(Foo.class, foo, "foo.id", 2);
        Assert.assertEquals((Integer)2, foo.getFoo().getId());

        operator.writeProperty(Foo.class, foo, "foo.foo.id", 3);
        Assert.assertEquals((Integer)3, foo.getFoo().getFoo().getId());

        operator.writeProperty(Foo.class, foo, "foo.foo.foo.id", 4);
        Assert.assertEquals((Integer)4, foo.getFoo().getFoo().getFoo().getId());

        // do nothing
        operator.writeProperty(Foo.class, null, "foo.foo.foo.foo.id", 4);
        operator.writeProperty(Foo.class, foo, "foo.foo.foo.foo.id", 4);
    }

    @Test
    public void findSetter() {
        Foo foo = new Foo(null, new Foo(null, new Foo(null, new Foo(null, null))));

        MethodInvoker setter = operator.findSetter(Foo.class, "id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 1);
        Assert.assertEquals((Integer)1, foo.getId());

        setter = operator.findSetter(Foo.class, "foo.id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 2);
        Assert.assertEquals((Integer)2, foo.getFoo().getId());

        setter = operator.findSetter(Foo.class, "foo.foo.id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 3);
        Assert.assertEquals((Integer)3, foo.getFoo().getFoo().getId());

        setter = operator.findSetter(Foo.class, "foo.foo.foo.id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 4);
        Assert.assertEquals((Integer)4, foo.getFoo().getFoo().getFoo().getId());

        // do nothing
        setter = operator.findSetter(Foo.class, "id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 5);
        operator.writeProperty(Foo.class, foo, "foo.foo.foo.foo.id", 4);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Foo {
        private Integer id;
        private Foo foo;
    }
}
