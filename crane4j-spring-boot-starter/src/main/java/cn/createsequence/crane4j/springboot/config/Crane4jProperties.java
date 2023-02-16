package cn.createsequence.crane4j.springboot.config;

import cn.createsequence.crane4j.core.annotation.ContainerConstant;
import cn.createsequence.crane4j.core.annotation.ContainerEnum;
import cn.createsequence.crane4j.core.annotation.ContainerMethod;
import cn.createsequence.crane4j.core.container.ConstantContainer;
import cn.createsequence.crane4j.core.parser.BeanOperationParser;
import cn.createsequence.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.createsequence.crane4j.core.support.reflect.PropertyOperator;
import cn.createsequence.crane4j.springboot.support.AnnotationMethodContainerProcessor;
import cn.createsequence.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.createsequence.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

/**
 * 可配置属性
 *
 * @author huangchengxing
 * @see Crane4jInitializer
 */
@ConfigurationProperties(prefix = Crane4jProperties.CRANE_PREFIX)
@Data
@Accessors(chain = true)
public class Crane4jProperties {

    public static final String CRANE_PREFIX = "crane4j";

    /**
     * <p>是否启用基于{@link com.esotericsoftware.reflectasm}的反射增强功能。
     * 启用一定程度上可以提升性能，但是可能会增加额外的内存占用。<br />
     * 若注册了自定义的{@link PropertyOperator}将会覆盖该配置。
     *
     * @see AsmReflectPropertyOperator
     */
    private boolean enableAsmReflect = false;

    /**
     * <p>扫描指定包路径，将该路径下的枚举适配并注册为数据源容器，
     * 若只需扫描被{@link ContainerEnum}注解的枚举，则一同配置{@link #onlyLoadAnnotatedEnum}。<br />
     * 比如：{@code com.example.constant.enum.*}
     *
     * @see ContainerEnum
     * @see ConstantContainer#forEnum
     */
    private Set<String> containerEnumPackages;

    /**
     * 是否只加载被{@link ContainerEnum}注解的枚举
     */
    private boolean onlyLoadAnnotatedEnum = false;

    /**
     * <p>扫描指定包路径，将该路径下被{@link ContainerConstant}注解的常量类适配并注册为数据源容器。
     * 比如：{@code com.example.constant.enum.*}
     *
     * @see ContainerConstant
     * @see ConstantContainer#forConstantClass
     */
    private Set<String> containerConstantPackages;

    /**
     * <p>扫描指定包路径下的所有类，使用容器中的配置解析器对其进行预解析。
     * 该配置有利于提高某些具备缓存功能的配置解析器的效率。<br />
     * 比如：{@code com.example.entity.*}
     *
     * @see BeanOperationParser
     */
    private Set<String> operateEntityPackages;

    /**
     * 是否启用方法参数自动填充切面
     *
     * @see MethodArgumentAutoOperateAspect
     */
    private boolean enableMethodArgumentAutoOperate = true;

    /**
     * 是否启用方法返回值自动填充切面
     *
     * @see MethodResultAutoOperateAspect
     */
    private boolean enableMethodResultAutoOperate = true;

    /**
     * 是否自动扫描并注册被{@link ContainerMethod}注解的方法为数据源容器
     *
     * @see AnnotationMethodContainerProcessor
     */
    private boolean enableMethodContainer = true;
    
    /**
     * 声明哪些数据源需要包装为缓存，
     * 格式为{@code 缓存名称: 容器的命名空间}
     */
    private Map<String, Set<String>> cacheContainers;
}
