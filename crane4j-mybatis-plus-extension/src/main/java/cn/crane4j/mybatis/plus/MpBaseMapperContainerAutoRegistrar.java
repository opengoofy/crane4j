package cn.crane4j.mybatis.plus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Auto registrar of container based on {@link BaseMapper}.
 *
 * @author huangchengxing
 */
@Accessors(chain = true)
@RequiredArgsConstructor
public class MpBaseMapperContainerAutoRegistrar implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final Crane4jMybatisPlusProperties properties;

    /**
     * After initializing all singleton beans in the Spring context,
     * obtain and parse the beans that implement the {@link BaseMapper} interface,
     * and then adapt them to {@link MpMethodContainer} and register them.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void afterSingletonsInstantiated() {
        Set<String> includes = properties.getIncludes();
        Set<String> excludes = properties.getExcludes();
        includes.removeAll(excludes);
        BiPredicate<String, BaseMapper<?>> mapperFilter = includes.isEmpty() ?
            (n, m) -> !excludes.contains(n) : (n, m) -> includes.contains(n) && !excludes.contains(n);
        Map<String, BaseMapper> mappers = applicationContext.getBeansOfType(BaseMapper.class);
        MpBaseMapperContainerRegister register = applicationContext.getBean(MpBaseMapperContainerRegister.class);
        mappers.entrySet().stream()
            .filter(e -> mapperFilter.test(e.getKey(), e.getValue()))
            .forEach(e -> register.registerMapper(e.getKey(), e.getValue()));
    }
}
