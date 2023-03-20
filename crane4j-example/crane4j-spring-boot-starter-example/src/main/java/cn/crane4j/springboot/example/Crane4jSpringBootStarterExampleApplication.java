package cn.crane4j.springboot.example;

import cn.crane4j.springboot.annotation.EnableCrane4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author huangchengxing
 */
@EnableCrane4j
@SpringBootApplication
public class Crane4jSpringBootStarterExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Crane4jSpringBootStarterExampleApplication.class, args);
    }
}
