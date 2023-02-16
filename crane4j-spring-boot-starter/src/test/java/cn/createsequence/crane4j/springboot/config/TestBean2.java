package cn.createsequence.crane4j.springboot.config;

import cn.createsequence.crane4j.core.annotation.Assemble;
import cn.createsequence.crane4j.core.annotation.Mapping;
import lombok.Data;

/**
 * @author huangchengxing
 */
@Data
public class TestBean2 {
    @Assemble(namespace = "test2", props = @Mapping(ref = "name"))
    private Integer id;
    private String name;
}
