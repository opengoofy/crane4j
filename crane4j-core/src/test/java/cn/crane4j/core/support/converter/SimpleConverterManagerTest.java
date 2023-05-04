package cn.crane4j.core.support.converter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;

/**
 * test for {@link SimpleConverterManager}
 *
 * @author huangchengxing
 */
public class SimpleConverterManagerTest {

    @Test
    @SuppressWarnings("all")
    public void getConverter() {
        SimpleConverterManager simpleConverterManager = new SimpleConverterManager();
        // force cast object to collection
        @SuppressWarnings("rawtypes")
        BiFunction<Object, Collection, Collection> converter = simpleConverterManager
            .getConverter(Object.class, Collection.class);
        Assert.assertNotNull(converter);
        Assert.assertEquals(Collections.emptyList(), converter.apply(Collections.emptyList(), null));
        Assert.assertEquals(Collections.emptyList(), converter.apply(null, Collections.emptyList()));
        Assert.assertEquals(SimpleConverterManagerTest.class, converter.apply(SimpleConverterManagerTest.class, null));
    }

    @Test
    @SuppressWarnings("all")
    public void convert() {
        ConverterManager converterManager = new SimpleConverterManager();
        Assert.assertEquals(Collections.emptyList(), converterManager.convert(Collections.emptyList(), Collection.class, null));
        Assert.assertEquals(Collections.emptyList(), converterManager.convert(null, Collection.class, Collections.emptyList()));
        Assert.assertEquals(SimpleConverterManagerTest.class, converterManager.convert(SimpleConverterManagerTest.class, Collection.class, null));
        Assert.assertNull(converterManager.convert(null, Collection.class, null));
        Assert.assertNull(converterManager.convert(null, Collection.class));
    }
}
