package cn.crane4j.core.support.auto;

import cn.hutool.core.collection.CollectionUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link ComposableAutoOperateAnnotatedElementResolver}.
 *
 * @author huangchengxing
 */
public class ComposableAutoOperateAnnotatedElementResolverTest {

    private static final AutoOperateAnnotatedElementResolver TEST_RESOLVER = (e, a) -> AutoOperateAnnotatedElement.EMPTY;

    @Test
    public void test() {
        ComposableAutoOperateAnnotatedElementResolver resolver = new ComposableAutoOperateAnnotatedElementResolver(
            CollectionUtil.newArrayList(TEST_RESOLVER)
        );
        Assert.assertEquals(1, resolver.getResolvers().size());
        resolver.addResolver(TEST_RESOLVER);
        Assert.assertEquals(1, resolver.getResolvers().size());
        resolver.removeResolver(TEST_RESOLVER);
        Assert.assertEquals(0, resolver.getResolvers().size());

        resolver.addResolver(TEST_RESOLVER);
        Assert.assertSame(AutoOperateAnnotatedElement.EMPTY, resolver.resolve(null, null));
        Assert.assertTrue(resolver.support(null, null));
    }
}
