package cn.crane4j.mybatis.plus;

import cn.crane4j.springboot.annotation.EnableCrane4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author huangchengxing
 */
@MapperScan
@EnableCrane4jMybatisPlusExtension
@EnableCrane4j
@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
