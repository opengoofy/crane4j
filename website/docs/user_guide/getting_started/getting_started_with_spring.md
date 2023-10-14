# 在 Spring 环境使用

下文 `crane4j` 的版本号 `${last-version}` 即为当前项目最新版本 ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

## 1.安装

引入 Spring 相关依赖，并引入 `crane4j-extension-spring` 即可。

~~~xml
<!-- crane4j 依赖 -->
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-extension-spring</artifactId>
    <version>${last-version}</version>
</dependency>

<!-- Spring 依赖 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.2.10.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>5.2.10.RELEASE</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>

<!-- 用于生成构造方法与 getter/setter 方法 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

## 2.启用配置

`crane4j` 已经准备好了默认的 Spring 配置类 `DefaultCrane4jSpringConfiguration`，用户仅需在自己的项目通过下述任意方式将其纳入 Spring 容器管理即可。

比如，你可以将 `DefaultCrane4jSpringConfiguration` 作为一个普通的 Bean 交给 Spring 管理：

~~~java
@Configuration
public class ProjectConfiguration {
    
    // 在 Spring 容器中声明一个默认配置类
    @Bean
    public DefaultCrane4jSpringConfiguration defaultCrane4jSpringConfiguration() {
        return new DefaultCrane4jSpringConfiguration();
    }
}
~~~

或者，你也可以在任意注解或配置类中通过 `@Import` 将配置引入：

~~~java
@Import(DefaultCrane4jSpringConfiguration.class)
@Configuration
public class ProjectConfiguration {
}
~~~

两种方式效果一样，项目启动后，`crane4j` 相关组件将会注册到 Spring 上下文中。

:::tip

目前没有提供 XML 格式的配置文件，不过，你可以参考 `DefaultCrane4jSpringConfiguration` 进行配置。

:::

## 3.配置数据源

在开始填充对象之前，你需要提前准备好一些数据源，并将其注册到全局配置对象中。

在 crane4j 中，一个数据源对应一个数据源容器（`Container`），它们通过独一无二的命名空间 （`namespace`）进行区分。

我们可以基于一个 `Map` 集合创建数据源容器，并将其注册到全局配置中：

~~~java
// 从 Spring 容器中获取全局配置
@Autowired
private Crane4jGlobalConfiguration configuration;

// 基于 Map 集合创建一个数据源容器
Map<Integer, String> map = new HashMap<>();
map.put(1, "a");
map.put(2, "b");
map.put(3, "c");
Container<Integer> container = Containers.forMap("test", map);

// 将数据源容器注册到全局配置中
configuration.registerContainer(container);
~~~

在后续通过命名空间 `test` 即可引用该数据源容器。

## 4.配置填充操作

接着，我们在需要填充的类属性上添加注解：

~~~java
@Data  // 使用 lombok 生成构造器、getter/setter 方法
@RequiredArgsConstructor
public static class Foo {
     // 根据 id 填充 name
    @Assemble(container = "test", props = @Mapping(ref = "name"))
    private final Integer id;
    private String name;
}
~~~

该配置表示，根据 id 值从容器中获取对应的数据源，并将其填充到 name 属性上。

## 5.触发填充

与非 Spring 环境不同，在 Spring 环境中，你可以选择手动填充或自动填充：

**手动填充**

~~~java
// 注入填充工具类
@Autowired
private OperateTemplate operateTemplate;

// 手动执行填充
List<Foo> foos = Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
operateTemplate.execute(foos);
System.out.println(foos);
// [Foo(id=1, name="a"), Foo(id=2, name="b"), Foo(id=3, name="c")]
~~~

**自动填充**

~~~java
// 在方法上添加注解，表明需要自动填充其方法返回值
@Component
public static class Service {
    @AutoOperate(type = Foo.class) // 类型为 Foo，返回值可以是 Collection 集合、数值或单个对象
    public List<Foo> getFoos() {
        return Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
    }
}

// 注入 Service，确保 AOP 成功拦截到该注解方法
@Autowired
private Service service;

// 自动填充方法返回值
List<Foo> foos = service.getFoos();
System.out.println(foos);
~~~

## 6.完整代码

~~~java
@Configuration
@Import(DefaultCrane4jSpringConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class QuickStartWithSpringTest {

    @Autowired
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private OperateTemplate operateTemplate;
    @Autowired
    private Service service;

    @Test
    public void run() {
        // 创建并注册数据源容器
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");
        Container<Integer> container = Containers.forMap("test", map);
        configuration.registerContainer(container);

        // 手动填充
        List<Foo> foos = Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
        operateTemplate.execute(foos);
        System.out.println(foos);

        // 自动填充
        foos = service.getFoos();
        System.out.println(foos);
    }

    @Component
    public static class Service {
        @AutoOperate(type = Foo.class)
        public List<Foo> getFoos() {
            return Arrays.asList(new Foo(1), new Foo(2), new Foo(3));
        }
    }

    @Data  // 使用 lombok 生成构造器、getter/setter 方法
    @RequiredArgsConstructor
    public static class Foo {
        // 根据 id 填充 name
        @Assemble(container = "test", props = @Mapping(ref = "name"))
        private final Integer id;
        private String name;
    }
}
~~~

