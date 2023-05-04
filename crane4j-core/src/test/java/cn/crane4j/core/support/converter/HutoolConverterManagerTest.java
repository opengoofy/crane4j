package cn.crane4j.core.support.converter;

import cn.crane4j.core.util.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;

/**
 * test for {@link HutoolConverterManager}
 *
 * @author huangchengxing
 */
public class HutoolConverterManagerTest {

    @Test
    public void getConverter() {
        HutoolConverterManager hutoolConverterManager = new HutoolConverterManager();
        // force cast object to collection
        hutoolConverterManager.getConverter(Object.class, Collection.class);
        @SuppressWarnings("rawtypes")
        BiFunction<Object, Collection, Collection> converter = hutoolConverterManager
            .getConverter(Object.class, Collection.class);
        Assert.assertNotNull(converter);

        Object obj = new Object();
        Assert.assertEquals(CollectionUtils.newCollection(ArrayList::new, obj), converter.apply(obj, null));
        Assert.assertEquals(converter.apply(Collections.singleton(obj), null), converter.apply(Collections.singleton(obj), null));
    }

    @Test
    public void convert() {
        ConverterManager converterManager = new HutoolConverterManager();
        Object obj = new Object();
        Assert.assertEquals(CollectionUtils.newCollection(ArrayList::new, obj), converterManager.convert(obj, Collection.class, null));
        Assert.assertEquals(converterManager.convert(Collections.singleton(obj), Collection.class, null), converterManager.convert(Collections.singleton(obj), Collection.class, null));
        Assert.assertNull(converterManager.convert(null, Collection.class));
    }
}
