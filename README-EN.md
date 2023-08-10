[English](https://github.com/opengoofy/crane4j/blob/dev/README-EN.md) | [中文](https://github.com/opengoofy/crane4j/blob/dev/README.md)

<img src="https://user-images.githubusercontent.com/49221670/221162632-95465432-f2df-4286-a53a-af59d70b1958.png" alt="image-20230220150040070" style="zoom: 80%;" />

![codecov](https://img.shields.io/badge/license-Apache--2.0-green) [![codecov](https://codecov.io/gh/opengoofy/crane4j/branch/dev/graph/badge.svg?token=CF2Q60Q0VH)](https://codecov.io/gh/opengoofy/crane4j) ![stars](https://img.shields.io/github/stars/Createsequence/crane4j) ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

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

~~~xml
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-extension-spring</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

**Enable the Framework**

~~~java
@EnableCrane4j // Enable crane4j
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
~~~

**Add Data Source**

~~~java
@Autowired
public Crane4jGlobalConfiguration configuration; // Inject global configuration

@PostConstruct
public void init() {
    // Create a data source based on a Map cache
    Map<Integer, String> sources = new HashMap<>();
    sources.put(0, "Female");
    sources.put(1, "Male");
    Container<Integer> genderContainer = Containers.forMap("gender", sources);
    configuration.registerContainer(genderContainer); // Register it to the global configuration
}
~~~

**Declare Population Operation**

~~~java
@RequiredArgsConstructor
public class Foo {
    @Assemble(
        container = "gender", // Use the data source named "gender"
        props = @Mapping(ref = "sexName") // Map the value based on 'code' to 'name'
    )
    private final Integer code; // Get the corresponding value based on 'code'
    private String name;
}
~~~

**Perform Population**

~~~java
@Autowired
public OperateTemplate operateTemplate; // Inject the rapid population utility class

// Populate objects using the utility class
List<Foo> foos = Arrays.asList(new Foo(0), new Foo(1));
operateTemplate.execute(foos);
System.out.println(foos);
// { "code": "0", "name": "Female" }
// { "code": "1", "name": "Male" }
~~~

This is the simplest way to use `crane4j` in a Spring Boot environment. For more usage details, please refer to the official documentation.

## Friendly Links

- [[hippo4j\]](https://gitee.com/agentart/hippo4j): A powerful dynamic thread pool framework with monitoring and alerting capabilities.

## Contribution and Technical Support

If you encounter any issues during usage, discover bugs, or have great ideas, feel free to raise your issues or [join the community discussion group](https://opengoofy.github.io/crane4j/#/other/%E8%81%94%E7%B3%BB%E4%BD%9C%E8%80%85.html)!

If you're unable to access the link or the WeChat group QR code is invalid, you can also contact the author to join the group:

![Contact Author](https://foruda.gitee.com/images/1678072903420592910/c0dbb802_5714667.png)
