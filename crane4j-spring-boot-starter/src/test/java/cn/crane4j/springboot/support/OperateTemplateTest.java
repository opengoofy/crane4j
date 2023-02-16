package cn.crane4j.springboot.support;

import cn.crane4j.core.annotation.Assemble;
import cn.crane4j.core.annotation.Disassemble;
import cn.crane4j.core.annotation.Mapping;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * test for {@link OperateTemplate}
 *
 * @author huangchengxing
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Crane4jAutoConfiguration.class)
public class OperateTemplateTest {

    @Autowired
    private OperateTemplate template;
    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private BeanOperationParser beanOperationParser;
    @Autowired
    private TypeResolver typeResolver;
    @Autowired
    private DisorderedBeanOperationExecutor beanOperationExecutor;

    @Before
    public void init() {
        Map<String, String> sources = new HashMap<>();
        sources.put("1", "1");
        sources.put("2", "2");
        sources.put("3", "3");
        context.registerContainer(ConstantContainer.forMap("test", sources));
    }

    @Test
    public void test() {
        List<Foo> fooList = getFooList();
        template.execute(fooList);
        checkBean(fooList.get(0), "1", "1", "1");

        fooList = getFooList();
        template.execute(fooList, op -> op instanceof AssembleOperation);
        checkBean(fooList.get(0), "1", null, null);

        fooList = getFooList();
        template.execute(fooList, beanOperationExecutor, op -> op instanceof AssembleOperation);
        checkBean(fooList.get(0), "1", null, null);

        fooList = getFooList();
        template.executeIfMatchAnyGroups(fooList, "nested");
        checkBean(fooList.get(0), null, "1", "1");

        fooList = getFooList();
        template.executeIfNoneMatchAnyGroups(fooList, "id");
        checkBean(fooList.get(0), null, null, "1");

        fooList = getFooList();
        template.executeIfMatchAllGroups(fooList, "id");
        checkBean(fooList.get(0), "1", null, null);
    }

    private static void checkBean(Foo foo, String name, String nestedName, String value) {
        Assert.assertEquals(foo.getName(), name);
        Assert.assertEquals(((NestedFoo)foo.getNestedFoo()).getName(), nestedName);
        Assert.assertEquals(((NestedFoo)foo.getNestedFoo()).getValue(), value);
    }

    private static List<Foo> getFooList() {
        return Arrays.asList(
            new Foo("1", new NestedFoo("1", "1")),
            new Foo("2", new NestedFoo("2", "2"))
        );
    }

    @Assemble(namespace = "test", props = @Mapping(ref = "name"))
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface AssembleId {
        @AliasFor(annotation = Assemble.class, attribute = "groups")
        String[] groups() default {};
    }

    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @AssembleId(groups = "id")
        private final String id;
        private String name;
        @Disassemble(groups = {"nested", "nestedFoo"})
        private final Object nestedFoo;
    }

    @RequiredArgsConstructor
    @Data
    private static class NestedFoo {
        @AssembleId(groups = {"nested", "id"})
        private final String id;
        private String name;
        @Assemble(
            namespace = "test", props = @Mapping(ref = "value"), groups = {"nested", "key"}
        )
        private final String key;
        private String value;
    }
}
