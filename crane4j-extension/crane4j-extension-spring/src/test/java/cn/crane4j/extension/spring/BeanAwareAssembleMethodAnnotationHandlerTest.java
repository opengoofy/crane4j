package cn.crane4j.extension.spring;

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.support.OperateTemplate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * test for {@link BeanAwareAssembleMethodAnnotationHandler}.
 *
 * @author huangchengxing
 */
@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
    DefaultCrane4jSpringConfiguration.class, BeanAwareAssembleMethodAnnotationHandlerTest.Config.class
})
public class BeanAwareAssembleMethodAnnotationHandlerTest {

    @Autowired
    private OperateTemplate operateTemplate;

    @Configuration
    protected static class Config {
        @Primary
        @Bean("nameSource")
        public NameSource nameSource() {
            return new NameSource();
        }
        @Bean("valueSource")
        public ValueSource valueSource() {
            return new ValueSource();
        }
    }

    @Test
    public void test() {
        List<Foo> targets = IntStream.range(0, 5)
            .mapToObj(Foo::new)
            .collect(Collectors.toList());
        operateTemplate.execute(targets);
        for (int i = 0; i < targets.size(); i++) {
            Foo target = targets.get(i);
            Assert.assertEquals("value" + i, target.getCode1());
            Assert.assertEquals("name" + i, target.getCode2());
            Assert.assertEquals("name" + i, target.getCode3());
            Assert.assertEquals("value" + i, target.getCode4());
            Assert.assertEquals("code" + i, target.getCode5());
        }
    }

    @Data
    @RequiredArgsConstructor
    protected static class Foo {

        @AssembleMethod( // is not a spring bean, use codeSource
            targetType = CodeSource.class,
            method = @ContainerMethod(bindMethod = "getData", resultType = Source.class),
            props = @Mapping(src = "code", ref = "code5")
        )
        @AssembleMethod( // find by beanName, use valueSource
            target = "valueSource",
            method = @ContainerMethod(bindMethod = "getData", resultType = Source.class),
            props = @Mapping(src = "code", ref = "code4")
        )
        @AssembleMethod( // find by type, use nameSource
            target = "cn.crane4j.extension.spring.BeanAwareAssembleMethodAnnotationHandlerTest$NameSource",
            method = @ContainerMethod(bindMethod = "getData", resultType = Source.class),
            props = @Mapping(src = "code", ref = "code3")
        )
        @AssembleMethod( // find primary bean, use nameSource
            targetType = Source.class,
            method = @ContainerMethod(bindMethod = "getData", resultType = Source.class),
            props = @Mapping(src = "code", ref = "code2")
        )
        @AssembleMethod( // find by name and type, use bean valueSource
            target = "valueSource", targetType = Source.class,
            method = @ContainerMethod(bindMethod = "getData", resultType = Source.class),
            props = @Mapping(src = "code", ref = "code1")
        )
        private final Integer id;
        private String code1;
        private String code2;
        private String code3;
        private String code4;
        private String code5;
    }

    protected static class NameSource extends Source {
        public List<Source> getData(Collection<Integer> ids) {
            return ids.stream()
                .map(id -> new Source().setId(id).setCode("name" + id))
                .collect(Collectors.toList());
        }
    }

    protected static class ValueSource extends Source {
        public List<Source> getData(Collection<Integer> ids) {
            return ids.stream()
                .map(id -> new Source().setId(id).setCode("value" + id))
                .collect(Collectors.toList());
        }
    }

    public static class CodeSource extends Source {
        public List<Source> getData(Collection<Integer> ids) {
            return ids.stream()
                .map(id -> new Source().setId(id).setCode("code" + id))
                .collect(Collectors.toList());
        }
    }

    @Accessors(chain = true)
    @Data
    protected static class Source {
        private Integer id;
        private String code;
    }
}
