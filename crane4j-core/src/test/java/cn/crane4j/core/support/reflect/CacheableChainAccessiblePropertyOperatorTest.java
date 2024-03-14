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
 * test for {@link CacheableChainAccessiblePropertyOperator}.
 *
 * @author huangchengxing
 */
public class CacheableChainAccessiblePropertyOperatorTest {

    private CacheableChainAccessiblePropertyOperator operator;

    @Before
    public void init() {
        operator = new CacheableChainAccessiblePropertyOperator(
            new ReflectivePropertyOperator(new HutoolConverterManager()),
            new ChainAccessiblePropertyOperator.DefaultSplitter(".")
        );
        operator = new CacheableChainAccessiblePropertyOperator(
            new ReflectivePropertyOperator(new HutoolConverterManager())
        );
    }

    @Test
    public void readProperty() {
        Foo foo = new Foo(1, new Foo(2, new Foo(3, new Foo(4, null))));
        Assert.assertNull(operator.readProperty(Foo.class, foo, "foo.foo.foo.foo.id"));
        Assert.assertNull(operator.readProperty(Foo.class, foo, "foo.foo.none.foo.id"));
        Assert.assertNull(operator.readProperty(Foo.class, foo, "foo.foo.foo.foo.none"));
        Assert.assertNull(operator.readProperty(Foo.class, new Foo(1, new Foo(2, null)), "foo.foo.foo.foo.id"));
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
        Assert.assertEquals(4, getter.invoke(foo));

        getter = operator.findGetter(Foo.class, "foo.foo.id");
        Assert.assertNotNull(getter);
        Assert.assertNull(getter.invoke(new Foo(1, null)));
        Assert.assertEquals(3, getter.invoke(foo));
        Assert.assertEquals(3, getter.invoke(foo));
        Assert.assertNull(getter.invoke(new Foo(1, null)));

        getter = operator.findGetter(Foo.class, "foo.id");
        Assert.assertNotNull(getter);
        Assert.assertEquals(2, getter.invoke(foo));
        Assert.assertEquals(2, getter.invoke(foo));

        getter = operator.findGetter(Foo.class, "id");
        Assert.assertNotNull(getter);
        Assert.assertEquals(1, getter.invoke(foo));
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
        operator.writeProperty(Foo.class, foo, "foo.foo.none.foo.id", 4);
        operator.writeProperty(Foo.class, foo, "foo.foo.none.foo.none", 4);
    }

    @Test
    public void findSetter() {
        Foo foo = new Foo(null, new Foo(null, new Foo(null, new Foo(null, null))));

        MethodInvoker setter = operator.findSetter(Foo.class, "id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 1);
        Assert.assertEquals((Integer)1, foo.getId());
        setter.invoke(foo, 11);
        Assert.assertEquals((Integer)11, foo.getId());

        setter = operator.findSetter(Foo.class, "foo.id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 2);
        Assert.assertEquals((Integer)2, foo.getFoo().getId());
        setter.invoke(foo, 22);
        Assert.assertEquals((Integer)22, foo.getFoo().getId());

        setter = operator.findSetter(Foo.class, "foo.foo.id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 3);
        Assert.assertEquals((Integer)3, foo.getFoo().getFoo().getId());
        setter.invoke(foo, 33);
        Assert.assertEquals((Integer)33, foo.getFoo().getFoo().getId());

        setter = operator.findSetter(Foo.class, "foo.foo.foo.id");
        Assert.assertNotNull(setter);
        Assert.assertNull(setter.invoke(new Foo(null, new Foo(null, null))));
        setter.invoke(foo, 4);
        Assert.assertEquals((Integer)4, foo.getFoo().getFoo().getFoo().getId());
        Assert.assertNull(setter.invoke(new Foo(null, new Foo(null, null))));
        setter.invoke(foo, 44);
        Assert.assertEquals((Integer)44, foo.getFoo().getFoo().getFoo().getId());

        // do nothing
        setter = operator.findSetter(Foo.class, "id");
        Assert.assertNotNull(setter);
        setter.invoke(foo, 5);
        setter.invoke(foo, 55);
        operator.writeProperty(Foo.class, foo, "foo.foo.foo.foo.id", 4);
        operator.writeProperty(Foo.class, foo, "foo.foo.foo.foo.id", 44);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Foo {
        private Integer id;
        private Foo foo;
    }
}
