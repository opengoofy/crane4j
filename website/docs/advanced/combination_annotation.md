# 组合注解

在 Spring 环境中，可以尝试使用 Spring 的组合注解机制优化 `crane4j` 的注解配置。

比如，原本你有如下配置：

~~~java
public class Student {
    @Assemble(
        key = "id", container = "student", 
        props = @Mapping(src = "studentName", ref = "name")
    )
    private Integer id;
    private String name;
}
~~~

当有多个处地方需要进行重复配置时，你可以使用组合注解机制优化它。

**创建组合注解**

~~~java
// 将目标注解作为元注解
@Assemble(key = "id", container = "student", props = @Mapping(src = "studentName", ref = "name"))
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleStudent { }
~~~

**使用组合注解**

然后直接使用组合注解 `@AssembleStudent` 代替原本的复杂配置：

~~~java
public class Student {
    @AssembleStudent
    private Integer id;
    private String name;
}
~~~

`crane4j` 几乎所有的注解都支持组合注解机制。

实际上，在非 Spring 环境，你也可以通过定制 `AnnotationFinder` 实现类似的功能，扩展自己的逻辑。

:::warning

注意，不支持解析复数可重复的组合注解，即若 `A` 是可重复注解，则当在一个元素上添加多个 `A` 时，将无法正确解析到该注解。

:::