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
     * <p>The field what in the target object to reference from the data source object.<br />
     * This field cannot be an empty string.
     * If it is not specified, it should point to the key field.
     *
     * @return field name
     */
    String getReference();
}
