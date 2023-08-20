[English](https://github.com/opengoofy/crane4j/blob/dev/README-EN.md) | [中文](https://github.com/opengoofy/crane4j/blob/dev/README.md)

<img src="https://user-images.githubusercontent.com/49221670/221162632-95465432-f2df-4286-a53a-af59d70b1958.png" alt="image-20230220150040070" style="zoom: 80%;" />

![codecov](https://img.shields.io/badge/license-Apache--2.0-green) [![codecov](https://codecov.io/gh/opengoofy/crane4j/branch/dev/graph/badge.svg?token=CF2Q60Q0VH)](https://codecov.io/gh/opengoofy/crane4j) ![stars](https://img.shields.io/github/stars/Createsequence/crane4j) ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

# Crane4j

强大又好用的数据填充框架，用少量注解搞定一切“根据 A 的 key 值拿到 B，再把 B 的属性映射到 A”的需求。

![image-20230810233647099](http://img.xiajibagao.top/image-20230810233647099.png)

## 它是什么？

在日常的开发工作中，我们经常面临着繁琐的数据组装任务：**根据一个对象的某个属性值，获取相关联的数据，并将其映射到另一个对象的属性中**。这种需求经常涉及到字典项、配置项、枚举常量，甚至需要进行关联数据库表的查询。这样的数据填充任务往往耗费大量时间和精力，而且容易产生重复的样板代码，让人感到心烦。

`crane4j` 旨在为了解决这种烦恼而生，它是一套注解驱动的数据填充框架。通过简单的注解配置，`crane4j` 可以优雅高效地完成不同数据源、不同类型、不同命名的字段填充任务，让你专注于核心业务逻辑而不再被繁琐的数据组装工作所困扰。

## 它有哪些特性？

- **多样的数据源支持**：支持枚举、键值对缓存和方法作为数据源，也可通过简单的自定义扩展兼容更多类型的数据源，并提供对所有数据源的缓存支持；
- **强大的字段映射能力**：通过注解即可完成不同类型字段的自动映射转换，还支持包括模板、排序、分组和嵌套对象填充等功能；
- **高度可扩展**：用户可以自由替换所有主要组件，结合 Spring 的依赖注入可实现轻松优雅的自定义扩展；
- **丰富的可选功能**：提供额外的自动填充方法返回值和方法入参参数，多线程填充，自定义注解和表达式，数据库框架插件等可选功能；
- **开箱即用**：简单配置即可与 spring/springboot 快速集成，也支持在非 spring 环境中使用；

## 文档

项目文档 document：[GitHub](https://opengoofy.github.io/crane4j/#/) / [Gitee](https://createsequence.gitee.io/crane4j-doc/#/)

## 快速开始

**引入依赖**

~~~xml
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-spring-boot-starter</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

**启用框架**

通过在启动类/配置类上添加 `@EnableCrane4j` 即可开启自动配置，在此处也可以直接配置枚举和常量类的扫描路径：

~~~java
@EnableCrane4j(
    constantPackages = "com.example.demo", // 描路常量类
    enumPackages = "com.example.demo"  // 扫描枚举类
)
@SpringBootApplicationpublic 
public class Application {   
    public static void main(String[] args) {  
        SpringApplication.run(Application.class, args); 
    }
}
~~~

**配置数据源**

`crane4j` 可以将**方法、枚举、常量、表达式、各种 ORM 框架甚至待填充对象本身都作为数据源**，这里以方法、枚举和常量三者为例：

~~~java
@Component
public void OperationDataSource {
    
    // 1、直接将实例方法作为数据源 "method"
    @ContainerMethod(namespace = "method", resultType = Foo.class)
    public List<Foo> getFooList(Set<Integer>ids) {
        return ids.stream()
            .map(id -> new Foo(id).setName("foo" + id))
            .collect(Collectors.toList());
    }

    // 2、将被扫描的枚举类作为数据源 "enum"
    @ContainerEnum(namespace = "enum", key = "code")
    @Getter
    @RequiredArgsConstructor
    public enum Gender {
        MALE(1, "男性"),
        FEMALE(0, "女性");
        private final Integer code;
        private final String name;
    }

    // 3、将被扫描的常量类作为数据源 "constant"
    @ContainerConstant(namespace = "constant", reverse = true)
    public static final class Constant {
        public static final String A = "1";
        public static final String B = "2";
        public static final String C = "3";
    }
}
~~~

**声明填充操作**

通过在字段添加注解即可基于上文配置的数据源声明填充操作，支持一对一、一对多甚至多对多的属性映射：

~~~java
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public static class Foo {

    // 1、根据id从方法中获取对应的对象，然后将其name映射到当前的name中
    @Assemble(container = "method", props = @Mapping("name"))
    private final Integer id;
    private String name;

    // 2、将自己的name属性映射到fooName
    @Assemble(props = @Mapping(src = "name", ref = "fooName"))
    private String fooName;

    // 3、根据gender获得对应的枚举对象，然后将其name属性映射到当前的genderName中
    @Assemble(
        container = "enum", props = @Mapping(src = "name", ref = "genderName")
    )
    private Integer gender;
    private String genderName;

    // 4、根据key集合从常量中批量获得对应的值，再将其批量映射到当前的value中
    @Assemble(
        container = "constant", props = @Mapping(ref = "values"),
        handlerType = ManyToManyAssembleOperationHandler.class
    )
    private Set<String> keys;
    private List<String> values;
}
~~~

**执行填充**

通过 `OperateTemplate` 即可快速完成填充，也可以在方法上添加 `@AutoOperate` 自动方法的返回值，这里以手动填充为例：

~~~java
@Autowired
public OperateTemplate operateTemplate; // 注入快速填充工具类

public void doOperate() {
    List<Foo> targets = IntStream.rangeClosed(1, 2)
        .mapToObj(id -> new Foo(id)
			.setGender(id & 1)
			.setKeys(CollectionUtils.newCollection(LinkedHashSet::new, "1", "2", "3"))
        ).collect(Collectors.toList());
    // 填充对象
    operateTemplate.execute(targets);
}
~~~

结果：

~~~json
[
    {
        "id": 1,
        "name": "foo1",
        "fooName": "foo1",
        "gender": 1,
        "genderName": "男性",
        "keys": ["1", "2", "3"],
        "values": ["A", "B", "C"]
    },
    {
        "id": 2,
        "name": "foo2",
        "fooName": "foo2",
        "gender": 0,
        "genderName": "女性",
        "keys": ["1", "2", "3"],
        "values": ["A", "B", "C"]
    }
]
~~~

这就是在 springboot 环境中使用 `crane4j` 的最简单步骤，更多玩法请参见官方文档。

## 友情链接

- [[ hippo4j \]](https://gitee.com/agentart/hippo4j)：强大的动态线程池框架，附带监控报警功能；

## 参与贡献和技术支持

如果在使用中遇到了问题、发现了 bug ，又或者是有什么好点子，欢迎提出你的 issues ，或者[加入社区交流群](https://opengoofy.github.io/crane4j/#/other/%E8%81%94%E7%B3%BB%E4%BD%9C%E8%80%85.html) 讨论！

若无法访问连接，或者微信群二维码失效，也可以联系作者加群：

![联系作者](https://foruda.gitee.com/images/1678072903420592910/c0dbb802_5714667.png)
