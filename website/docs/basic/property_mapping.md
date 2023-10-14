# 配置属性映射

<img src="https://img.xiajibagao.top/image-20230220182129822.png" alt="image-20230220182129822" style="zoom: 50%;" />

当你通过 key 从容器中获取到对应的数据后，属性配置将决定它们之间将如何映射字段值。

对应到代码中有点像 `MapStruct`，一个 `@Mapping` 表示一组字段间的映射关系，一个 `@Assemble` 会有包含多个 `@Mapping` 注解。

该注解属性如下：

| 属性  | 指向             | 为空时指向         |
| ----- | ---------------- | ------------------ |
| `src` | 数据源对象的属性 | 指向数据源对象本身 |
| `ref` | 带填充对象的属性 | 指向 key 字段      |

不同的 `src` 与 `ref` 组合将会产生不同的效果。

## 1.属性到属性

当 `src` 与 `ref` 皆不为空时表示两个将 a 的属性值映射到 b 的属性值。比如：

```java
public class StudentVO {
    @Assemble(
        container = "student", props = {
            @Mapping(src = "studentName", ref = "name"), // student.studentName -> studentVO.name
            @Mapping(src = "studentClassName", ref = "className") // student.studentClassName -> studentVO.className
        }
    )
    private Integer id;
    private String name;
    private String className;
}
```

上述示例中，我们通过`@Assemble`注解指定了数据源容器和字段映射。其中，`props` 属性中使用了 `@Mapping` 注解配置了两个映射属性，分别：

+ 将`Student.studentName`映射到`StudentVO.name`；
+ 将`Student.studentClassName`映射到`StudentVO.className`。

### 1.1.同名属性

另外，如果 `src` 和 `ref` 指定的字段名称相同，可以直接在`value`中同时指定。比如：

```java
public class StudentVO {
    @Assemble(
        container = "student", 
        props = @Mapping("name") // student.name -> studentVO.name
    )
    private Integer id;
    private String name;
}
```

### 1.2.自动类型转换

当两边的属性类型不同时，将会自动进行类型转换，该功能依赖 `ConverterManager` 实现，具体参见后文 “[类型转换](./../advanced/type_converter.md)” 一节。

## 2.对象到属性

当在 `@Mapping` 注解中不指定 `src` 时，表示直接将整个数据源对象映射到目标对象的属性上。比如：

```java
public class StudentVO {
    @Assemble(
        container = "student", 
        props = @Mapping(ref = "student") // student -> studentVO.student
    )
    private Integer id;
    private Student student;
}
```

上述示例中，根据 `id` 查找到 `Student` 对象后，直接将该 `Student` 对象赋值给`StudentVO.student` 字段。

这种配置通常适用于对象的组装，或者当数据源对象本身就是某个字典值或枚举值的情况。

## 3.属性到键

当在 `@Mapping` 注解中不指定引用字段 `ref` 时，表示直接将 `src` 指向的属性值映射到 `key` 字段上。比如：

```java
public class StudentVO {
    @Assemble(
        container = "gender", 
        props = @Mapping(src = "name")  // gender.name -> studentVO.gender
    )
    private String gender;
}
```

上述示例中，根据 `StudentVO.sex` 查找到性别字典对象 `Gender` 后，将其对应的 `Gender.name` 映射回 `StudentVO.sex` 字段。

这种配置适用于将字典值或枚举值映射回目标对象的字段上。

## 4.对象到键

当在 `@Mapping` 注解中不指定引用字段 `ref` ，且不指定 `src` 时，表示整个数据源对象映射到目标对象的 `key` 字段上。比如：

```java
public class StudentVO {
    @Assemble(container = "gender") // 假设通过 gender 获得的数据为 Map<String, String> 格式，比如 {key = "male", value = "男"}
    private String sexgender
}
```

这种情况比较罕见。

## 5.批量映射

在一些情况下，从数据源容器获得的一个键值将对应一批数据源对象（比如一对多或者多对多装配），在这种情况下，字段映射将变为**批量映射**模式。

具体而言，对于集合或数组中的每个数据源对象，我们会从中获取指定的  `src`  属性值，并将所有属性值组装为集合，然后将该集合赋值给目标对象中的  `ref`  属性。

例如：

```java
public class StudentVO {
    @Assemble(
        container = "teacher",
        handler = "manyToManyAssembleOperationHandler",
        // [teacher, teacher...] -> [teacher.name, teacher.name...] -> studentVO.teacherNames
        props = @Mapping(src = "name", ref = "teacherNames") 
    )
    private String teacherIds; // 以逗号分隔的字符串，例如：1, 2, 3
    private List<String> teacherNames;
}
```

上述示例中，根据 `teacherIds` 字段字符串中通过逗号分隔的多个键值，查询关联的多个 `Teacher` 对象，然后将 `Teacher` 对象集合的 `name` 属性映射为 `List<String>` 并赋值给 `StudentVO.teacherNames` 字段。

该字段映射遵循普通字段映射的语义，例如对象映射：

```java
public class StudentVO {
    @Assemble(
        container = "teacher", 
        handler = "manyToManyAssembleOperationHandler",
        props = @Mapping(ref = "teachers")
    )
    private List<Integer> teacherIds; // 也可以直接是集合或者数组
    private List<Teacher> teachers;
}
```

在批量映射的情况下，返回的对象可以是数据源对象或数据源对象的属性集合。

该功能需要配合装配处理器 `AssembleOperationHandler` 使用，具体参见后文 “[一对一&多对多](./assemble_operation_handler.md)” 一节。

:::warning

- 如果用户传入的字符串是 `id1, id2, id3...` 这样的格式，分割后的值默认保持为 `String` 类型。用户需要确保目标容器能够接受 `String` 类型的键；
- 如果分隔符不是逗号 `,`，或者有其他拆分规则，用户可以为 `ManyToManyAssembleOperationHandler` 设置自定义的字符串分割器；

:::

## 6.属性映射模板

为了保持代码的整洁性，我们可以将字段映射配置抽取为独立的模板。比如，现有如下配置：

```java
public class StudentVO {
    @Assemble(
        container = "student", 
        props = {
            @Mapping(src = "studentName", ref = "name"),
            @Mapping(src = "studentClassName", ref = "className"),
            @Mapping(src = "studentTeacherName", ref = "teacherName")
        }
    )
    private Integer id;
    private String name;
    private String className;
    private String teacherName;
}
```

我们可以创建一个模板类 `StudentMappingTemplate`，将 `props` 中的映射配置移到模板类中：

```java
@MappingTemplate({
    @Mapping(src = "studentName", ref = "name"),
    @Mapping(src = "studentClassName", ref = "className"),
    @Mapping(src = "studentTeacherName", ref = "teacherName")
})
private static class StudentMappingTemplate {}
```

然后，在 `@Assemble` 注解中使用 `propTemplates` 引入模板：

```java
public class StudentVO {
    @Assemble(
        container = "student",
        props = @Mapping(src = "studentTeacherAge", ref = "teacherAge"),
        propTemplates = StudentMappingTemplate.class
    )
    private Integer id;
    private String name;
    private String className;
    private String teacherName;
    private Integer teacherAge;
}
```

通过模板引入的字段映射配置与通过 `@Assemble.props` 声明的配置效果相同，并且两者可以同时存在。

字段映射规则按照**就近原则**执行。即离 `StudentVO` 越近，且排序靠前的字段优先完成映射，后面的映射字段会覆盖已有的值。

## 7.链式操作符

`crane4j` 支持在 `@Mapping` 注解中，通过类似 js 的链式操作符的方式来访问或设置嵌套对象的属性。

例如，我们有一个 `Foo` 类如下：

```java
public class FooVO {
    @Assemble(container = "foo", props = @Mapping(src = "name", ref = "nested.name"))
    private Integer id;
    private NestedFoo nested;
}

public class NestedFoo {
    private String name;
}

public class Foo {
    private Integer id;
    private String name;
}
```

我们声明了一个装配操作，假设 `foo` 容器返回的数据源对象是 `Foo`，则上述示例表示，获取到 `Foo` 对象后，将 `Foo.name` 映射到 `FooVO` 对象中 `nested` 属性中的嵌套对象 `NestedFoo` 的 `name` 属性。

:::warning

该功能是通过 `ChainAccessiblePropertyOperator` 类实现的。如果用户替换了默认的 `PropertyOperator`，则需要手动使用 `ChainAccessiblePropertyOperator` 对用户的实现进行包装。

具体参见后文 “[反射工厂](./../advanced/reflection_factory.md)” 部分内容。

:::
