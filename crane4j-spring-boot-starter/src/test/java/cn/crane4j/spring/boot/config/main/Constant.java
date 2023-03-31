package cn.crane4j.spring.boot.config.main;

import cn.crane4j.annotation.ContainerConstant;

/**
 * @author huangchengxing
 */
@ContainerConstant(namespace = "constant")
public class Constant {
    @ContainerConstant.Name("one")
    public static final String ONE = "one";
    @ContainerConstant.Name("two")
    public static final String TWO = "two";
    @ContainerConstant.Exclude
    public static final String THREE = "three";
}
