package cn.crane4j.core.support;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * test for {@link SimpleTypeResolver}
 *
 * @author huangchengxing
 */
public class SimpleTypeResolverTest {

    @Test
    public void resolve() {
        TypeResolver resolver = new SimpleTypeResolver();
        Assert.assertNull(resolver.resolve(null));

        Foo foo = new Foo();
        Assert.assertEquals(Foo.class, resolver.resolve(foo));

        Foo[] fooArray = {null, new Foo()};
        Assert.assertEquals(Foo.class, resolver.resolve(fooArray));

        List<Foo> fooList = Arrays.asList(null, new Foo());
        Assert.assertEquals(Foo.class, resolver.resolve(fooList));
    }

    private static class Foo {}

}
