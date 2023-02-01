package cn.createsequence.crane4j.core.parser;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;

/**
 * {@link PropertyMapping}的简单实现
 *
 * @author huangchengxing
 */
public class SimplePropertyMapping implements PropertyMapping {

    @Getter
    private final String source;
    private final boolean hasSource;
    @Getter
    private final String reference;

    /**
     * 创建一个字段映射配置
     *
     * @param source 数据源对象中的字段
     * @param reference 目标对象中要引用数据源对象中字段的字段
     */
    public SimplePropertyMapping(String source, String reference) {
        this.source = source;
        this.hasSource = CharSequenceUtil.isNotEmpty(source);
        this.reference = reference;
    }

    /**
     * {@link #getSource()}是否为空
     *
     * @return 是否
     */
    @Override
    public boolean hasSource() {
        return hasSource;
    }

}
