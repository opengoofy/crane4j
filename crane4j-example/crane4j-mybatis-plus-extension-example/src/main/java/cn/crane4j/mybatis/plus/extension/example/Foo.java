package cn.crane4j.mybatis.plus.extension.example;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author huangchengxing
 */
@TableName("foo")
@Data
public class Foo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String userName;

    @TableField("age")
    private Integer userAge;

    @TableField("sex")
    private Integer userSex;
}
