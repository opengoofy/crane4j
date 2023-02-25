package cn.crane4j.springboot.config;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.springboot.support.AnnotationMethodContainerProcessor;
import cn.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

/**
 * Configurable properties.
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
     * <p>Whether to enable reflection enhancement based on {@link com.esotericsoftware.reflectasm}.
     * Enabling can improve performance to some extent, but may increase additional memory usage.<br />
     * If the customized {@link PropertyOperator} is registered, the configuration will be overwritten.
     *
     * @see AsmReflectPropertyOperator
     */
    private boolean enableAsmReflect = false;

    /**
     * <p>Scan the specified package path, adapt the enumeration
     * under the path and register it as a data source container.<br />
     * For example: {@code com.example.instant.enum.*}.
     *
     * <p>If only need to scan the enumeration annotated by
     * {@link ContainerEnum}, set {@link #onlyLoadAnnotatedEnum} is {@code true}.
     *
     * @see ContainerEnum
     * @see ConstantContainer#forEnum
     */
    private Set<String> containerEnumPackages;

    /**
     * Whether to load only the enumeration annotated by {@link ContainerEnum}.
     */
    private boolean onlyLoadAnnotatedEnum = false;

    /**
     * <p>Scan the specified package path, adapt the constant class annotated by
     * {@link ContainerConstant} under the path and register it as a data source container.<br />
     * For example: {@code com.example.instant.enum.*}.
     *
     * @see ContainerConstant
     * @see ConstantContainer#forConstantClass
     */
    private Set<String> containerConstantPackages;

    /**
     * <p>Scan all classes under the specified package path and pre-parse them
     * using the configuration parser in the spring context.<br />
     * For example: {@code com.example.entity.*}.
     *
     * <p>This configuration is conducive to improving the efficiency
     * of some configuration parsers with cache function.
     *
     * @see BeanOperationParser
     */
    private Set<String> operateEntityPackages;

    /**
     * Whether to enable automatic filling of aspect with method parameters.
     *
     * @see MethodArgumentAutoOperateAspect
     */
    private boolean enableMethodArgumentAutoOperate = true;

    /**
     * Whether to enable the method return value to automatically fill the cut surface.
     *
     * @see MethodResultAutoOperateAspect
     */
    private boolean enableMethodResultAutoOperate = true;

    /**
     * Whether to automatically scan and register the method
     * annotated by {@link ContainerMethod} as the data source container.
     *
     * @see AnnotationMethodContainerProcessor
     */
    private boolean enableMethodContainer = true;
    
    /**
     * Declare which data sources need to be packaged as caches in the format {@code cache name: namespace of container}.
     */
    private Map<String, Set<String>> cacheContainers;
}
