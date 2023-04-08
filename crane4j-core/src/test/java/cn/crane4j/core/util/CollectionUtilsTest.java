package cn.crane4j.core.util;

import com.google.common.collect.Iterators;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link CollectionUtils}
 *
 * @author huangchengxing
 */
public class CollectionUtilsTest {

    @Test
    public void newWeakConcurrentMap() {
        Assert.assertNotNull(CollectionUtils.newWeakConcurrentMap());
    }

    @Test
    public void adaptObjectToCollection() {
        Assert.assertTrue(CollectionUtils.adaptObjectToCollection(null).isEmpty());
        Assert.assertTrue(CollectionUtils.adaptObjectToCollection(new Object[0]).isEmpty());
        Assert.assertTrue(CollectionUtils.adaptObjectToCollection(Collections.emptyList()).isEmpty());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection(Collections.emptyMap()).size());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection(new Object()).size());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection(Iterators.singletonIterator(1)).size());
        Assert.assertEquals(1, CollectionUtils.adaptObjectToCollection((Iterable<?>)() -> Iterators.singletonIterator(1)).size());
    }
}
