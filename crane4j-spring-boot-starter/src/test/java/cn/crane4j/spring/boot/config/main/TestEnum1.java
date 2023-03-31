package cn.crane4j.spring.boot.config.main;

import cn.crane4j.annotation.ContainerEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author huangchengxing
 */
@ContainerEnum(namespace = "test1", key = "code")
@Getter
@RequiredArgsConstructor
public enum TestEnum1 {

    ONE(1, "one"),
    TWO(2, "two"),
    THREE(3, "three");

    private final int code;
    private final String value;
}
