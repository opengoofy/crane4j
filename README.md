<img src="https://user-images.githubusercontent.com/49221670/221162632-95465432-f2df-4286-a53a-af59d70b1958.png" alt="image-20230220150040070" style="zoom: 80%;" />

![codecov](https://img.shields.io/badge/license-Apache--2.0-green) [![codecov](https://codecov.io/gh/opengoofy/crane4j/branch/dev/graph/badge.svg?token=CF2Q60Q0VH)](https://codecov.io/gh/opengoofy/crane4j) ![stars](https://img.shields.io/github/stars/Createsequence/crane4j) ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

# Crane4j

强大又好用的数据填充框架，用少量注解搞定一切“根据 A 的 key 值拿到 B，再把 B 的属性映射到 A”的需求。

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
    <artifactId>crane4j-extension-spring</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

**启用框架**

~~~java
@EnableCrane4j // 启用 crane4j
@SpringBootApplicationpublic 
public class Application {   
    public static void main(String[] args) {  
        SpringApplication.run(Application.class, args); 
    }
}
~~~

**添加数据源**

~~~java
@Autowrite
public Crane4jGlobalConfiguration configuration; // 注入全局配置

@PostConst
public void init() {
    // 基于 Map 缓存，创建一个数据源
    Map<Integer, String> sources = new HashMap<>();
    sources.put(0, "女");
    sources.put(1, "男");
    Container<String> genderContainer = Containers.forMap("gender", sources);
    configuration.registerContainer(genderContainer); // 将其注册到全局配置中
}
~~~

**声明填充操作**

~~~java
@RequireArgsConstructor
public class Foo {
    @Assemble(
        container = "gender", // 使用名为的 gender 数据源
        props = @Mapping(ref = "sexName") // 将根据 code 取得值映射到 name 上
    )
    private final Integer code; // 根据 code 获得对应的值
    private String name;
}
~~~

**执行填充**

~~~java
@Autowrite
public OperateTemplate operateTemplate; // 注入快速填充工具类

// 使用工具类填充对象
List<Student> foos = Array.asList(new Foo(0), new Foo(1));
operateTemplate.execute(foos);
System.out.println(foos);
// { "code": "0", "name": "女" }
// { "code": "1", "name": "男" }
~~~

这就是在 springboot 环境中使用 `crane4j` 的最简单步骤，更多玩法请参见官方文档。

## 友情链接

- [[ hippo4j \]](https://gitee.com/agentart/hippo4j)：强大的动态线程池框架，附带监控报警功能；

## 参与贡献和技术支持

如果在使用中遇到了问题、发现了 bug ，又或者是有什么好点子，欢迎提出你的 issues ，或者[加入社区交流群](https://opengoofy.github.io/crane4j/#/other/%E8%81%94%E7%B3%BB%E4%BD%9C%E8%80%85.html) 讨论！

若无法访问连接，或者微信群二维码失效，也可以联系作者加群：

![联系作者](https://foruda.gitee.com/images/1678072903420592910/c0dbb802_5714667.png)