package cn.crane4j.mybatis.plus;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.MethodInvoker;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link Container} implemented by {@link BaseMapper#selectList} method based on {@link BaseMapper}.
 *
 * @author huangchengxing
 */
@AllArgsConstructor
public class MpMethodContainer<T> implements Container<Object> {

    @Getter
    private final String namespace;
    private final BaseMapper<T> baseMapper;
    private String[] queryColumns;
    private final String key;
    private final MethodInvoker keyExtractor;

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        QueryWrapper<T> wrapper = getQueryWrapper(keys);
        // TODO support one to manay
        List<T> targets = baseMapper.selectList(wrapper);
        return targets.stream().collect(Collectors.toMap(
            keyExtractor::invoke, Function.identity()
        ));
    }

    private QueryWrapper<T> getQueryWrapper(Collection<Object> keys) {
        QueryWrapper<T> wrapper = Wrappers.<T>query().in(key, keys);
        if (queryColumns.length > 0) {
            wrapper.select(queryColumns);
        }
        return wrapper;
    }
}
