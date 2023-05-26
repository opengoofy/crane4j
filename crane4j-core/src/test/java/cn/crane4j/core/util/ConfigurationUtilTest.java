package cn.crane4j.core.util;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * test for {@link ConfigurationUtil}
 *
 * @author huangchengxing
 */
public class ConfigurationUtilTest {

    @Test
    public void createPropertyMapping() {
        MappingTemplate mappingTemplate = AnnotatedElement.class.getAnnotation(MappingTemplate.class);

        PropertyMapping propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[0]);
        Assert.assertEquals("name", propertyMapping.getSource());
        Assert.assertEquals("name", propertyMapping.getReference());

        propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[1]);
        Assert.assertEquals("address", propertyMapping.getSource());
        Assert.assertEquals("", propertyMapping.getReference());
        Assert.assertTrue(propertyMapping.hasSource());

        propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[2]);
        Assert.assertEquals("", propertyMapping.getSource());
        Assert.assertEquals("age", propertyMapping.getReference());
        Assert.assertFalse(propertyMapping.hasSource());

        propertyMapping = ConfigurationUtil.createPropertyMapping(mappingTemplate.value()[3]);
        Assert.assertEquals("sex", propertyMapping.getSource());
        Assert.assertEquals("sex", propertyMapping.getReference());
    }

    @Test
    public void parsePropTemplate() {
        MappingTemplate mappingTemplate = AnnotatedElement.class.getAnnotation(MappingTemplate.class);
        List<PropertyMapping> mappings = ConfigurationUtil.parsePropTemplate(mappingTemplate);
        Assert.assertEquals(4, mappings.size());
    }

    @Test
    public void parsePropTemplateClasses() {
        List<PropertyMapping> mappings = ConfigurationUtil.parsePropTemplateClasses(
            new Class[]{ AnnotatedElement.class }, new SimpleAnnotationFinder()
        );
        Assert.assertEquals(4, mappings.size());
    }

    @MappingTemplate({
        @Mapping(src = "name", ref = "name"),
        @Mapping(src = "address"),
        @Mapping(ref = "age"),
        @Mapping("sex"),
    })
    private static class AnnotatedElement { }
}
