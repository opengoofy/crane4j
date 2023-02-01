package cn.createsequence.crane4j.core.annotation;

import cn.createsequence.crane4j.core.executor.DisassembleOperationHandler;
import cn.createsequence.crane4j.core.executor.ReflectDisassembleOperationHandler;
import cn.createsequence.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.createsequence.crane4j.core.parser.BeanOperationParser;
import cn.createsequence.crane4j.core.parser.DisassembleOperation;
import cn.createsequence.crane4j.core.parser.TypeDynamitedDisassembleOperation;
import cn.createsequence.crane4j.core.support.TypeResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>声明一个拆卸的操作，等同于一个{@link DisassembleOperation}。<br />
 * 指定当前对象的特定属性作为需要拆卸的嵌套对象。在装配操作开始之前，
 * 会优先执行类中声明的拆卸操作，此时会提取并摊平嵌套对象。
 *
 * <p>比如，我们在下例的类中声明了一个装配操作和一个拆卸操作：
 * <pre class="code">{@code
 * public class Foo {
 *     // 装配操作
 *     @Assemble(namespace = "test", props = @Mapping(src = "name"))
 *     private Integer id;
 *
 *     // 拆卸操作
 *     @Dissassemble(type = Foo.class)
 *     private List<Foo> fooList;
 * }
 * }</pre>
 * 当对对象进行处理时，将会先将该对象中的"fooList"展开并平铺，
 * 然后将"fooList"集合中的嵌套对象按照"Foo.class"的配置进行装配。
 *
 * <p>该注解可以支持处理集合、数组或单个对象。<br />
 * 此外，当待拆卸的字段无法确定类型——比如该字段类型为泛型——时，可以不指定类型，比如：
 * <pre class="code">{@code
 * public class Foo<T> {
 *     // 拆卸操作
 *     @Dissassemble
 *     private List<T> fooList;
 * }
 * }</pre>
 * 当后续处理时，将会动态推断对象实际类型(此处参见{@link TypeResolver 类型解析器}与{@link TypeDynamitedDisassembleOperation 动态拆卸})，
 * 不过相对固定类型会有额外的性能消耗。
 *
 * @author huangchengxing
 * @see DisassembleOperation
 * @see DisassembleOperationHandler
 * @see AnnotationAwareBeanOperationParser
 */
@Repeatable(value = Disassemble.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Disassemble {

    /**
     * 排序值，在同一个类中，值越小的装配操作会越优先执行
     *
     * @return 排序值
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * <p>带有需要拆卸的嵌套对象的字段。<br />
     * 当注解注解在属性上时，会强制指定为被注解属性的名称。
     *
     * @return key字段名
     */
    String key() default "";

    /**
     * <p>嵌套对象的类型，该值可填其父类型。<br />
     * 若对象为泛型，或无法确认，则默认为{@link Object}，将会自动推断类型。
     *
     * @return 嵌套对象的类型
     */
    Class<?> type() default Object.class;

    /**
     * 用于完成当前操作的拆卸操作处理器
     *
     * @return 拆卸操作处理器类型
     */
    Class<? extends DisassembleOperationHandler> handler() default ReflectDisassembleOperationHandler.class;

    /**
     * 用于解析嵌套对象操作配置的解析器类型
     *
     * @return 类型
     */
    Class<? extends BeanOperationParser> parser() default AnnotationAwareBeanOperationParser.class;

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
        Disassemble[] value();
    }
}
