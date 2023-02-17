package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>声明一个装配操作。<br />
 * 指定当前对象的特定属性作为key，当操作被执行时，将提取key值并从指定的数据源容器获得的数据源对象，
 * 并根据配置将数据元对象中指定的属性值映射到当前对象的相应属性。<br />
 * 这里给一个简单的例子：
 * <pre class="code">{@code
 * public class Foo {
 *     @Assemble(
 *         namespace = "test",
 *         props = @Mapping(ref = "name", src = "value")
 *     )
 *     private Integer id;
 *     private String name;
 * }
 * }</pre>
 * 上述例子表示：从"test"容器，根据id字段值获得对应的数据源对象，
 * 然后将该数据源对象的"value"字段值，映射到当前对象的"name"字段。
 *
 * <h3>字段映射配置</h3>
 * 通过{@link Mapping}进行字段映射配置，字段映射支持多种映射方式。
 * 比如：
 * 若现有待处理对象<i>T</i>，与对应的数据源对象<i>S</i>，若有：
 * <pre class="code">{@code
 * public class T {
 *     @Assemble
 *     private String id;
 *     private String name;
 * }
 * }</pre>
 * 则：
 * <table type="text">
 *     <tr><td>映射配置                                     </td><td><td>待映射的数据</td><td>目标属性</td></tr>
 *     <tr><td>{@code @Mapping(src = "name", ref = "name")}</td><td><td>S.name     </td><td>T.name</td></tr>
 *     <tr><td>{@code @Mapping(ref = "name")}              </td><td><td>S.name     </td><td>T.id</td></tr>
 *     <tr><td>{@code @Mapping(src = "name")}              </td><td><td>S          </td><td>T.name</td></tr>
 *     <tr><td>{@code @Mapping}                            </td><td><td>S          </td><td>T.id</td></tr>
 * </table>
 *
 * <h3>映射模板</h3>
 * <p>当{@link #props}配置的字段映射过多时，可以考虑通过{@link #propTemplates()}将其分离至模板。
 * 比如：
 * <pre class="code">{@code
 * // 将字段映射配置分离到模板类
 * @MappingTemplate(@Mapping(src = "id1", ref = "name1"))
 * private static class MappingTemp {}
 *
 * // 简化后
 * public class Foo {
 *     @Assemble(
 *         namespace = "test",
 *         propTemplates = MappingTemp.class // 引入模板类
 *     )
 *     private Integer id;
 *     private String name;
 * }
 * }</pre>
 * 当解析后，声明在{@link MappingTemplate}中的配置等同于直接在{@link #props}中声明。
 *
 * @author huangchengxing
 * @see cn.crane4j.core.executor.handler.AssembleOperationHandler;
 * @see cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
 * @see cn.crane4j.core.parser.AssembleOperation;
 */
@Repeatable(value = Assemble.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Assemble {

    /**
     * 排序值，在同一个类中，越小的装配操作会越优先执行
     *
     * @return 排序值
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * <p>key字段的字段名。<br />
     * 该字段的指端值将用于后续从数据源容器中获得关联的数据源对象。
     *
     * <p>当注解注解在属性上时，会强制指定为被注解属性的名称。
     *
     * @return key字段名
     */
    String key() default "";

    /**
     * 要使用的数据源容器的命名空间，当为空时，默认数据源对象即为当前实例本身
     *
     * @return 命名空间
     */
    String namespace() default "";

    /**
     * 处理器名称
     *
     * @return 处理器名称
     */
    String handlerName() default "";

    /**
     * 用于完成当前操作的装配操作处理器
     *
     * @return 装配操作处理器类型
     */
    Class<?> handler() default Object.class;

    /**
     * <p>需要在数据源对象与当前对象之间映射的属性值。<br />
     * 多需要映射的属性较多，可以将属性通过{@link #propTemplates()}以模板方式引入。
     *
     * @return 属性映射配置
     */
    Mapping[] props() default {};

    /**
     * <p>属性映射模板。<br />
     * 指定一个类，如果类上存在{@link MappingTemplate}，
     * 将会扫描并将该注解中的{@link Mapping}添加到{@link #props()}。
     *
     * <p>注意：模板中的属性始终在处理{@link #props()}中的属性之后进行处理。
     *
     * @return 带有注解的类
     */
    Class<?>[] propTemplates() default {};

    /**
     * 当前操作所属的组别
     *
     * @return 组别
     */
    String[] groups() default {};

    /**
     * 批量操作
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Assemble[] value() default {};
    }
}
