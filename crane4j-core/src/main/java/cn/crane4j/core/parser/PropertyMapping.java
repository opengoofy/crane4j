package cn.crane4j.core.parser;

/**
 * Mapping relationship of a pair of associated attributes between data source object and target object.
 *
 * @author huangchengxing
 */
public interface PropertyMapping {

    /**
     * The field in the data source object will be mapped to
     * the field in the target object corresponding to
     * {@link #getReference} after the operation is executed.
     *
     * @return field name
     */
    String getSource();

    /**
     * Whether {@link #getSource()} is empty.
     *
     * @return boolean
     */
    boolean hasSource();

    /**
     * <p>The field in the target object to reference the field
     * in the data source object will obtain the value of
     * the data source field corresponding to {@link #getSource}
     * in the data source after the operation is executed.<br />
     * This field cannot be an empty string. If it is not specified, it should point to the key field.
     *
     * @return field name
     */
    String getReference();
}
