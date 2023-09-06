[English](https://github.com/opengoofy/crane4j/blob/dev/README-EN.md) | [中文](https://github.com/opengoofy/crane4j/blob/dev/README.md)

<img src="https://user-images.githubusercontent.com/49221670/221162632-95465432-f2df-4286-a53a-af59d70b1958.png" alt="image-20230220150040070" style="zoom: 80%;" />

![codecov](https://img.shields.io/badge/license-Apache--2.0-green) [![codecov](https://codecov.io/gh/opengoofy/crane4j/branch/dev/graph/badge.svg?token=CF2Q60Q0VH)](https://codecov.io/gh/opengoofy/crane4j) [![star](https://gitee.com/opengoofy/crane4j/badge/star.svg?theme=dark)](https://gitee.com/opengoofy/crane4j/stargazers) ![stars](https://img.shields.io/github/stars/Createsequence/crane4j) ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

# Crane4j

A powerful and user-friendly data population framework that effortlessly handles all 'retrieve B based on key value from A, and then map properties of B to A' requirements with minimal annotations.

![image-20230810233647099](http://img.xiajibagao.top/image-20230810233647099.png)

## What is it?

In our daily development work, we often face the tedious task of assembling data: **retrieving associated data based on a property value of an object and mapping it to properties of another object**. This requirement often involves dictionaries, configuration items, enum constants, and even queries to related database tables. Such data population tasks can consume a significant amount of time and effort, and they tend to generate repetitive boilerplate code, causing frustration.

`crane4j` is designed to alleviate this frustration. It is an annotation-driven data population framework. Through simple annotation configurations, `crane4j` elegantly and efficiently handles various data sources, different types, and fields with varying names. This allows you to focus on core business logic without being burdened by the intricacies of data assembly.

## What are its features?

- **Diverse Data Source Support**: It supports enums, key-value pair caches, and methods as data sources. It can also be easily extended to accommodate more types of data sources through simple custom extensions, and provides caching support for all data sources.
- **Robust Field Mapping Capability**: Automatic mapping and conversion of different field types can be achieved through annotations. It also supports features such as templates, sorting, grouping, and nested object population.
- **Highly Extensible**: Users can freely replace all major components, allowing for easy and elegant custom extensions in conjunction with Spring's dependency injection.
- **Rich Optional Features**: It offers additional features like auto-populating method return values and method parameters, multi-threaded population, custom annotations and expressions, and database framework plugins.
- **Out-of-the-box**: It seamlessly integrates with Spring/Spring Boot through simple configurations and is also compatible for use in non-Spring environments.

## Documentation

Project documentation: [GitHub](https://opengoofy.github.io/crane4j/#/) / [Gitee](https://createsequence.gitee.io/crane4j-doc/#/)

## Quick Start

**Add Dependencies**

```xml
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-spring-boot-starter</artifactId>
    <version>${last-version}</version>
</dependency>
```

**Enable the Framework**

To enable automatic configuration, simply add `@EnableCrane4j` to your startup class or configuration class. You can also configure the scan paths for enums and constants at this point:

```java
@EnableCrane4j(
    constantPackages = "com.example.demo", // Scan constant classes
    enumPackages = "com.example.demo"  // Scan enum classes
)
@SpringBootApplication
public class Application {   
    public static void main(String[] args) {  
        SpringApplication.run(Application.class, args); 
    }
}
```

**Configure Data Sources**

`crane4j` allows you to use methods, enums, constants, expressions, various ORM frameworks, and even objects themselves as data sources. Here, we'll use methods, enums, and constants as examples:

```java
@Component
public void OperationDataSource {
    
    // 1. Use instance methods as a data source "method"
    @ContainerMethod(namespace = "method", resultType = Foo.class)
    public List<Foo> getFooList(Set<Integer>ids) {
        return ids.stream()
            .map(id -> new Foo(id).setName("foo" + id))
            .collect(Collectors.toList());
    }

    // 2. Use scanned enum classes as a data source "enum"
    @ContainerEnum(namespace = "enum", key = "code")
    @Getter
    @RequiredArgsConstructor
    public enum Gender {
        MALE(1, "Male"),
        FEMALE(0, "Female");
        private final Integer code;
        private final String name;
    }

    // 3. Use scanned constant classes as a data source "constant"
    @ContainerConstant(namespace = "constant", reverse = true)
    public static final class Constant {
        public static final String A = "1";
        public static final String B = "2";
        public static final String C = "3";
    }
}
```

**Declare Fill Operations**

You can declare fill operations based on the configured data sources by adding annotations to fields. It supports one-to-one, one-to-many, and even many-to-many property mappings:

```java
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public static class Foo {

    // 1. Get objects from methods based on id and map their name to the current name
    @Assemble(container = "method", props = @Mapping("name"))
    private final Integer id;
    private String name;

    // 2. Map your own name property to fooName
    @Assemble(props = @Mapping(src = "name", ref = "fooName"))
    private String fooName;

    // 3. Get the corresponding enum object based on gender and map its name to genderName
    @Assemble(
        container = "enum", props = @Mapping(src = "name", ref = "genderName")
    )
    private Integer gender;
    private String genderName;

    // 4. Get corresponding values from constants based on a key collection and map them to the current value
    @Assemble(
        container = "constant", props = @Mapping(ref = "values"),
        handlerType = ManyToManyAssembleOperationHandler.class
    )
    private Set<String> keys;
    private List<String> values;
}
```

**Perform Filling**

You can quickly perform filling using `OperateTemplate`. You can also add `@AutoOperate` to the return value of a method. Here's an example of manual filling:

```java
@Autowired
public OperateTemplate operateTemplate; // Inject the rapid filling utility class

public void doOperate() {
    List<Foo> targets = IntStream.rangeClosed(1, 2)
        .mapToObj(id -> new Foo(id)
			.setGender(id & 1)
			.setKeys(CollectionUtils.newCollection(LinkedHashSet::new, "1", "2", "3"))
        ).collect(Collectors.toList());
    // Fill objects
    operateTemplate.execute(targets);
}
```

Result:

```json
[
    {
        "id": 1,
        "name": "foo1",
        "fooName": "foo1",
        "gender": 1,
        "genderName": "Male",
        "keys": ["1", "2", "3"],
        "values": ["A", "B", "C"]
    },
    {
        "id": 2,
        "name": "foo2",
        "fooName": "foo2",
        "gender": 0,
        "genderName": "Female",
        "keys": ["1", "2", "3"],
        "values": ["A", "B", "C"]
    }
]
```

This is the simplest way to use `crane4j` in a Spring Boot environment. For more advanced usage, please refer to the official documentation.

## Friendly Links

- [[hippo4j\]](https://gitee.com/agentart/hippo4j): A powerful dynamic thread pool framework with monitoring and alerting capabilities.

## Contribution and Technical Support

If you encounter any issues during usage, discover bugs, or have great ideas, feel free to raise your issues or [join the community discussion group](https://opengoofy.github.io/crane4j/#/other/%E8%81%94%E7%B3%BB%E4%BD%9C%E8%80%85.html)!

If you're unable to access the link or the WeChat group QR code is invalid, you can also contact the author to join the group:

![Contact Author](https://foruda.gitee.com/images/1678072903420592910/c0dbb802_5714667.png)
