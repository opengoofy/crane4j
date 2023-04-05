package cn.crane4j.extension.mybatis.plus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author huangchengxing
 */
@Mapper
public interface FooMapper extends BaseMapper<Foo> {
}
