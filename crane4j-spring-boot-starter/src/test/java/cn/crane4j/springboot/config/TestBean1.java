package cn.crane4j.springboot.config;

import cn.crane4j.core.annotation.Assemble;
import cn.crane4j.core.annotation.Disassemble;
import cn.crane4j.core.annotation.Mapping;
import lombok.Data;

import java.util.List;

/**
 * @author huangchengxing
 */
@Data
public class TestBean1 {
    @Assemble(namespace = "test1", props = @Mapping(src = "code", ref = "name"))
    private Integer id;
    private String name;
    @Disassemble(type = TestBean2.class)
    private List<TestBean2> testBean2List;
}
