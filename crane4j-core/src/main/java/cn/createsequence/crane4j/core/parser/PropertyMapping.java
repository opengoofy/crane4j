package cn.createsequence.crane4j.core.parser;

/**
 * 数据源对象与目标对象之间一对关联属性的映射关系
 *
 * @author huangchengxing
 */
public interface PropertyMapping {

    /**
     * 数据源对象中的字段，在操作执行后，
     * 该字段将会被映射到{@link #getReference}所对应的目标对象中的字段。
     *
     * @return 数据源对象字段
     */
    String getSource();

    /**
     * {@link #getSource()}是否为空
     *
     * @return 是否
     */
    boolean hasSource();

    /**
     * <p>目标对象中要引用数据源对象中字段的字段，在操作执行后，
     * 该字段将会获得数据源中{@link #getSource}所对应的数据源字段的值。<br />
     * 该字段不允许为空/空字符串，当不指定时应当指向key字段。
     *
     * @return 引用字段
     */
    String getReference();
}
