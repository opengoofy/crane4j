package cn.crane4j.spring.boot.config.main;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import lombok.Data;

import java.util.List;

/**
 * @author huangchengxing
 */
@Data
public class TestBean1 {
    @Assemble(container = "test1", props = @Mapping(src = "code", ref = "name"))
    private Integer id;
    private String name;
    @Disassemble(type = TestBean2.class)
    private List<TestBean2> testBean2List;
}
