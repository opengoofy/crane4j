package cn.crane4j.core.util;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * test for {@link ConfigurationUtil}
 *
 * @author huangchengxing
 */
public class ConfigurationUtilTest {

    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        this.configuration = SimpleCrane4jGlobalConfiguration.create();
    }

    @Test
    public void getOperationParser() {
        Class<?> clazz = TypeHierarchyBeanOperationParser.class;
        Assert.assertNotNull(ConfigurationUtil.getOperationParser(configuration, clazz.getName(), null));
        Assert.assertNotNull(ConfigurationUtil.getOperationParser(configuration, null, clazz));
    }

    @Test
    public void getAssembleOperationHandler() {
        Class<?> clazz = OneToOneAssembleOperationHandler.class;
        Assert.assertNotNull(ConfigurationUtil.getAssembleOperationHandler(configuration, clazz.getName(), null));
        Assert.assertNotNull(ConfigurationUtil.getAssembleOperationHandler(configuration, null, clazz));
    }

    @Test
    public void getDisassembleOperationHandler() {
        Class<?> clazz = ReflectDisassembleOperationHandler.class;
        Assert.assertNotNull(ConfigurationUtil.getDisassembleOperationHandler(configuration, clazz.getName(), null));
        Assert.assertNotNull(ConfigurationUtil.getDisassembleOperationHandler(configuration, null, clazz));
    }

    @Test
    public void getOperationExecutor() {
        Class<?> clazz = DisorderedBeanOperationExecutor.class;
        Assert.assertNotNull(ConfigurationUtil.getOperationExecutor(configuration, clazz.getName(), null));
        Assert.assertNotNull(ConfigurationUtil.getOperationExecutor(configuration, null, clazz));
    }

    @Test
    public void getContainerProvider() {
        Class<?> clazz = ContainerProvider.class;
        Assert.assertNotNull(ConfigurationUtil.getContainerProvider(configuration, clazz.getName(), null));
        Assert.assertNotNull(ConfigurationUtil.getContainerProvider(configuration, null, clazz));
    }

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

    @Test
    public void invokeRegisterAware() {
        TestRecordContainerRegisterAware aware = new TestRecordContainerRegisterAware();
        Assert.assertNull(ConfigurationUtil.invokeRegisterAware(this, null, Collections.singletonList(aware), t -> {}));
        Assert.assertEquals(0, aware.getBeforeInvokeCount().intValue());
        Assert.assertEquals(0, aware.getAfterInvokeCount().intValue());

        Container<?> container = Container.empty();
        Assert.assertSame(container, ConfigurationUtil.invokeRegisterAware(this, Container.empty(), Collections.singletonList(aware), t -> {}));
        Assert.assertEquals(1, aware.getBeforeInvokeCount().intValue());
        Assert.assertEquals(1, aware.getAfterInvokeCount().intValue());

        aware.setReturnNull(true);
        Assert.assertNull(ConfigurationUtil.invokeRegisterAware(this, Container.empty(), Collections.singletonList(aware), t -> {}));
        Assert.assertEquals(2, aware.getBeforeInvokeCount().intValue());
        Assert.assertEquals(1, aware.getAfterInvokeCount().intValue());
    }

    @Getter
    private static class TestRecordContainerRegisterAware implements ContainerRegisterAware {
        @Setter
        private boolean returnNull = false;
        private Integer beforeInvokeCount = 0;
        private Integer afterInvokeCount = 0;
        @Nullable
        @Override
        public Container<?> beforeContainerRegister(Object operator, @Nonnull Container<?> container) {
            beforeInvokeCount++;
            return returnNull ? null : container;
        }
        @Override
        public void afterContainerRegister(Object operator, @Nonnull Container<?> container) {
            afterInvokeCount++;
        }
    }

    @MappingTemplate({
        @Mapping(src = "name", ref = "name"),
        @Mapping(src = "address"),
        @Mapping(ref = "age"),
        @Mapping("sex"),
    })
    private static class AnnotatedElement { }
}
