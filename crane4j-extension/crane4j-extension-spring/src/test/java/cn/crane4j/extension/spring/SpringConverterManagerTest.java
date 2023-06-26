package cn.crane4j.extension.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.function.BiFunction;

/**
 * test for {@link SpringConverterManager}
 *
 * @author huangchengxing
 */
public class SpringConverterManagerTest {

    @Test
    public void test() {
        ConversionService conversionService = DefaultConversionService.getSharedInstance();
        SpringConverterManager converterManager = new SpringConverterManager(conversionService);
        Assert.assertSame(conversionService, converterManager.getConversionService());

        BiFunction<String, Integer, Integer> converter = converterManager.getConverter(String.class, Integer.class);
        Assert.assertNotNull(converter);
        Assert.assertEquals((Integer)1, converter.apply("1", 0));
        Assert.assertEquals((Integer)0, converter.apply("NaN", 0));
    }
}
