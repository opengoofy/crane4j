package cn.crane4j.mybatis.plus;

import cn.crane4j.springboot.config.Crane4jProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * Crane4j mybatis plus properties.
 *
 * @author huangchengxing
 */
@ConfigurationProperties(prefix = Crane4jMybatisPlusProperties.CRANE4J_MP_EXTENSION_PREFIX)
@Data
public class Crane4jMybatisPlusProperties {

    public static final String CRANE4J_MP_EXTENSION_PREFIX = Crane4jProperties.CRANE_PREFIX + ".mybatis-plus";

    /**
     * mapper allowed to be scanned and registered.
     */
    private Set<String> includes = new HashSet<>();

    /**
     * mapper not allowed to be scanned and registered.
     */
    private Set<String> excludes = new HashSet<>();

    /**
     * whether to register mapper automatically
     */
    private boolean autoRegisterMapper = false;
}
