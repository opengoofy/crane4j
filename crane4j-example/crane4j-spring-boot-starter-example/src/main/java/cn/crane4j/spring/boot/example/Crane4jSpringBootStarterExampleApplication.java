package cn.crane4j.spring.boot.example;

import cn.crane4j.spring.boot.annotation.EnableCrane4j;
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
