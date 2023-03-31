package cn.crane4j.spring.boot.config.main;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import lombok.Data;

/**
 * @author huangchengxing
 */
@Data
public class TestBean2 {
    @Assemble(container = "test2", props = @Mapping(ref = "name"))
    private Integer id;
    private String name;
}
