package cn.crane4j.mybatis.plus.extension.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author huangchengxing
 */
@MapperScan("cn.crane4j.mybatis.plus.extension.example")
//@EnableCrane4j
@SpringBootApplication
public class Crane4jMybatisPlusExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Crane4jMybatisPlusExampleApplication.class, args);
    }
}
