package cn.crane4j.core.parser;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;

/**
 * Simple implementation of {@link PropertyMapping}.
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
     * Create a property mapping configuration.
     *
     * @param source fields in the data source object
     * @param reference field in the target object to reference the field in the data source object
     */
    public SimplePropertyMapping(String source, String reference) {
        this.source = source;
        this.hasSource = CharSequenceUtil.isNotEmpty(source);
        this.reference = reference;
    }

    /**
     * Whether {@link #getSource()} is empty.
     *
     * @return boolean
     */
    @Override
    public boolean hasSource() {
        return hasSource;
    }
}
